package com.amarprojects.accounting.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amarprojects.accounting.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoiceInvoiceNumber(String invoiceNumber);
    Optional<List<Payment>> findPaymentsByInvoiceId(Long invoiceId);
    List<Payment> findByInvoiceId(Long invoiceId);
}
