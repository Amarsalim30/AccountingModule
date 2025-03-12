package com.amarprojects.accounting.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.amarprojects.accounting.dto.InvoiceRequest;
import com.amarprojects.accounting.dto.InvoiceResponse;
import com.amarprojects.accounting.exception.AccountingException;
import com.amarprojects.accounting.model.Account;
import com.amarprojects.accounting.model.JournalEntry;
import com.amarprojects.accounting.model.JournalEntryLine;
import com.amarprojects.accounting.repository.AccountRepository;
import com.amarprojects.accounting.repository.JournalEntryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceService {
    
    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;

    @Transactional
    public void createInvoice(InvoiceRequest request) {
        BigDecimal vat = calculateVat(request.getSubtotal(), request.getTaxRate());
        BigDecimal total = request.getSubtotal().add(vat);

        JournalEntry entry = new JournalEntry();
        entry.setLines(new ArrayList<>());
        entry.setDate(LocalDate.now());
        entry.setDescription("Invoice: " + request.getInvoiceNumber());

        Account arAccount = accountRepository.findByCode("AR")
                .orElseThrow(() -> new AccountingException("Accounts Receivable account not found"));
        
        Account revenueAccount = accountRepository.findByCode("REV")
                .orElseThrow(() -> new AccountingException("Revenue account not found"));
        
        Account vatAccount = accountRepository.findByCode("VAT")
                .orElseThrow(() -> new AccountingException("VAT Payable account not found"));

        addJournalLine(entry, arAccount, total, BigDecimal.ZERO);
        addJournalLine(entry, revenueAccount, BigDecimal.ZERO, request.getSubtotal());
        addJournalLine(entry, vatAccount, BigDecimal.ZERO, vat);

        validateJournalBalance(entry);
        journalEntryRepository.save(entry);
        log.info("Created journal entry for invoice: {}", request.getInvoiceNumber());
    }

    public InvoiceResponse findInvoice(String invoiceNumber) {
        JournalEntry entry = journalEntryRepository.findByDescriptionContaining(invoiceNumber)
                .orElseThrow(() -> new AccountingException("Invoice not found: " + invoiceNumber));
        
        return convertToResponse(entry);
    }

    public List<InvoiceResponse> listAllInvoices() {
        return journalEntryRepository.findByDescriptionStartsWith("Invoice: ")
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private InvoiceResponse convertToResponse(JournalEntry entry) {
        InvoiceResponse response = new InvoiceResponse();
        response.setDate(entry.getDate());
        response.setInvoiceNumber(entry.getDescription().replace("Invoice: ", ""));

        BigDecimal total = entry.getLines().stream()
                .filter(line -> line.getAccount().getCode().equals("AR"))
                .map(JournalEntryLine::getDebit)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        BigDecimal vat = entry.getLines().stream()
                .filter(line -> line.getAccount().getCode().equals("VAT"))
                .map(JournalEntryLine::getCredit)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        BigDecimal subtotal = entry.getLines().stream()
                .filter(line -> line.getAccount().getCode().equals("REV"))
                .map(JournalEntryLine::getCredit)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        response.setTotal(total);
        response.setVat(vat);
        response.setSubtotal(subtotal);

        return response;
    }

    private BigDecimal calculateVat(BigDecimal subtotal, BigDecimal taxRate) {
        return subtotal.multiply(taxRate).setScale(4, RoundingMode.HALF_EVEN);
    }

    private void addJournalLine(JournalEntry entry, Account account, BigDecimal debit, BigDecimal credit) {
        JournalEntryLine line = new JournalEntryLine();
        line.setJournalEntry(entry);
        line.setAccount(account);
        line.setDebit(debit);
        line.setCredit(credit);
        entry.getLines().add(line);
    }

    private void validateJournalBalance(JournalEntry entry) {
        BigDecimal totalDebit = entry.getLines().stream()
                .map(JournalEntryLine::getDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCredit = entry.getLines().stream()
                .map(JournalEntryLine::getCredit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new AccountingException("Journal entry is unbalanced: Debit " + 
                totalDebit + " ≠ Credit " + totalCredit);
        }
    }
   
}
