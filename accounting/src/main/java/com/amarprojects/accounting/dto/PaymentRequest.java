// src/main/java/com/amarprojects/accounting/dto/PaymentRequest.java
package com.amarprojects.accounting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    @NotNull(message = "Invoice ID is required")
    private Long invoiceId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be positive")
    private BigDecimal paymentAmount;

    private String paymentMethod;   // e.g., "CASH", "CREDIT_CARD", etc.
    private LocalDate paymentDate;  // Defaults to today if not provided
    private String transactionReference; // e.g., a unique reference from the payment gateway
}
