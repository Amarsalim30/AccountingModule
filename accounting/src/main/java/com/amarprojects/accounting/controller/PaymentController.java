package com.amarprojects.accounting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import com.amarprojects.accounting.dto.PaymentRequest;
import com.amarprojects.accounting.dto.PaymentResponse;
import com.amarprojects.accounting.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping
    public ResponseEntity<Void> processPayment(@Valid @RequestBody PaymentRequest request) {
        paymentService.processPayment(request);
        return ResponseEntity.ok().build();
    }
    //get payment history for a specific invoice
    @GetMapping("/{invoiceId}")
    public ResponseEntity<List<PaymentResponse>> findPaymentsByInvoiceId(@PathVariable Long invoiceId) {
        List<PaymentResponse> payments = paymentService.findPaymentsByInvoiceId(invoiceId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<PaymentResponse> payments = paymentService.listAllPayments();
        return ResponseEntity.ok(payments);
    }
}
