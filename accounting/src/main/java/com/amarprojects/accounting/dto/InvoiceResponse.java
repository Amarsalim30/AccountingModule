package com.amarprojects.accounting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class InvoiceResponse {
    private String invoiceNumber;
    private LocalDate date;
    private BigDecimal subtotal;
    private BigDecimal vat;
    private BigDecimal total;
}
