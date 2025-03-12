package com.amarprojects.accounting.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InvoiceRequest {
    @NotBlank
    private String invoiceNumber;
    
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal subtotal;
    
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    private BigDecimal taxRate;
}
