package com.amarprojects.accounting.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amarprojects.accounting.model.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}

