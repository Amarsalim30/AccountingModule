package com.amarprojects.accounting.repository;

import com.amarprojects.accounting.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoiceInvoiceNumber(String invoiceNumber);
}
