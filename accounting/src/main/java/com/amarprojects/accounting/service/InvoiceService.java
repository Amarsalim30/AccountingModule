package com.amarprojects.accounting.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amarprojects.accounting.dto.InvoiceRequest;
import com.amarprojects.accounting.dto.InvoiceResponse;
import com.amarprojects.accounting.exception.AccountingException;
import com.amarprojects.accounting.model.Account;
import com.amarprojects.accounting.model.Invoice;
import com.amarprojects.accounting.model.JournalEntry;
import com.amarprojects.accounting.model.JournalEntryLine;
import com.amarprojects.accounting.repository.AccountRepository;
import com.amarprojects.accounting.repository.InvoiceRepository;
import com.amarprojects.accounting.repository.JournalEntryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;

    @Transactional
    public Invoice createInvoice(InvoiceRequest request) {
        // Calculate tax and totals
        BigDecimal taxAmount = request.getSubtotal()
                .multiply(request.getTaxRate())
                .setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal totalAmount = request.getSubtotal().add(taxAmount);

        // Build Invoice entity
        Invoice invoice = Invoice.builder()
                .invoiceNumber(request.getInvoiceNumber())
                .invoiceDate(LocalDate.now())
                .subtotal(request.getSubtotal())
                .taxRate(request.getTaxRate())
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .outstandingAmount(totalAmount)
                .status("UNPAID")
                .build();

        // Save invoice record
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Create Journal Entry for the invoice
        JournalEntry entry = new JournalEntry();
        entry.setDate(LocalDate.now());
        entry.setDescription("Invoice: " + savedInvoice.getInvoiceNumber());

        // Debit: Accounts Receivable (AR) for the total amount
        Account arAccount = accountRepository.findByCode("AR")
                .orElseThrow(() -> new AccountingException("Accounts Receivable account not found"));
        addJournalLine(entry, arAccount, totalAmount, BigDecimal.ZERO);

        // Credit: Sales Revenue for the subtotal
        Account revenueAccount = accountRepository.findByCode("REV")
                .orElseThrow(() -> new AccountingException("Revenue account not found"));
        addJournalLine(entry, revenueAccount, BigDecimal.ZERO, request.getSubtotal());

        // Credit: VAT Payable for the tax amount
        Account vatAccount = accountRepository.findByCode("VAT")
                .orElseThrow(() -> new AccountingException("VAT Payable account not found"));
        addJournalLine(entry, vatAccount, BigDecimal.ZERO, taxAmount);

        // Validate and save Journal Entry
        validateJournalBalance(entry);
        journalEntryRepository.save(entry);

        log.info("Created invoice {} with total amount {}", savedInvoice.getInvoiceNumber(), totalAmount);
        return savedInvoice;
    }

    public InvoiceResponse findInvoice(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new AccountingException("Invoice not found: " + invoiceNumber));
        return convertToResponse(invoice);
    }

    public List<InvoiceResponse> listAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private InvoiceResponse convertToResponse(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setDate(invoice.getInvoiceDate());
        response.setSubtotal(invoice.getSubtotal());
        response.setVat(invoice.getTaxAmount());
        response.setTotal(invoice.getTotalAmount());
        return response;
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
