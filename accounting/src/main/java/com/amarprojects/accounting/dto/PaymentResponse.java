package com.amarprojects.accounting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String transactionReference;
    private String invoiceNumber;
}
