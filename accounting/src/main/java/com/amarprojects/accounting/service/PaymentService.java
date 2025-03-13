package com.amarprojects.accounting.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amarprojects.accounting.dto.PaymentRequest;
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
public class PaymentService {
    private static final String STATUS_PAID = "PAID";
    private static final String STATUS_PARTIALLY_PAID = "PARTIALLY_PAID";
    private static final String CASH_ACCOUNT_CODE = "CASH";
    private static final String AR_ACCOUNT_CODE = "AR";

    private final InvoiceRepository invoiceRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;

    @Transactional
    public void processPayment(PaymentRequest request) {
        validatePaymentRequest(request);
        Invoice invoice = getAndValidateInvoice(request.getInvoiceId());
        
        processPaymentAmount(invoice, request.getPaymentAmount());
        JournalEntry paymentEntry = createPaymentJournalEntry(invoice, request.getPaymentAmount());
        
        invoiceRepository.save(invoice);
        journalEntryRepository.save(paymentEntry);
        
        log.info("Processed payment of {} for Invoice: {}", request.getPaymentAmount(), invoice.getInvoiceNumber());
    }

    private void validatePaymentRequest(PaymentRequest request) {
        if (request == null) {
            throw new AccountingException("Payment request cannot be null");
        }
        if (request.getPaymentAmount() == null || request.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountingException("Invalid payment amount");
        }
        if (request.getInvoiceId() == null) {
            throw new AccountingException("Invoice ID is required");
        }
    }

    private Invoice getAndValidateInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AccountingException("Invoice not found with ID: " + invoiceId));
        
        if (STATUS_PAID.equals(invoice.getStatus())) {
            throw new AccountingException("Invoice is already fully paid");
        }
        return invoice;
    }

    private void processPaymentAmount(Invoice invoice, BigDecimal paymentAmount) {
        BigDecimal outstanding = invoice.getOutstandingAmount();
        if (paymentAmount.compareTo(outstanding) > 0) {
            throw new AccountingException("Payment amount exceeds outstanding invoice balance");
        }

        invoice.setOutstandingAmount(outstanding.subtract(paymentAmount));
        invoice.setStatus(determineInvoiceStatus(invoice.getOutstandingAmount()));
    }

    private String determineInvoiceStatus(BigDecimal remainingAmount) {
        return remainingAmount.compareTo(BigDecimal.ZERO) == 0 ? STATUS_PAID : STATUS_PARTIALLY_PAID;
    }

    private JournalEntry createPaymentJournalEntry(Invoice invoice, BigDecimal paymentAmount) {
        JournalEntry paymentEntry = new JournalEntry();
        paymentEntry.setDate(LocalDate.now());
        paymentEntry.setDescription("Payment for Invoice: " + invoice.getInvoiceNumber());

        Account cashAccount = accountRepository.findByCode(CASH_ACCOUNT_CODE)
                .orElseThrow(() -> new AccountingException("Cash account not found"));
        Account arAccount = accountRepository.findByCode(AR_ACCOUNT_CODE)
                .orElseThrow(() -> new AccountingException("Accounts Receivable account not found"));

        addJournalLine(paymentEntry, cashAccount, paymentAmount, BigDecimal.ZERO);
        addJournalLine(paymentEntry, arAccount, BigDecimal.ZERO, paymentAmount);
        
        validateJournalBalance(paymentEntry);
        return paymentEntry;
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
            throw new AccountingException("Payment journal entry is unbalanced: Debit " + 
                totalDebit + " ≠ Credit " + totalCredit);
        }
    }
}
