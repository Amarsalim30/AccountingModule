package com.amarprojects.accounting.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.amarprojects.accounting.dto.InvoiceRequest;
import com.amarprojects.accounting.dto.InvoiceResponse;
import com.amarprojects.accounting.service.InvoiceService;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoice Management")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new invoice")
    public void createInvoice(@Valid @RequestBody InvoiceRequest request) {
        invoiceService.createInvoice(request);
    }

    @GetMapping("/{invoiceNumber}")
    @Operation(summary = "Find invoice by number")
    public InvoiceResponse findInvoice(@PathVariable String invoiceNumber) {
        return invoiceService.findInvoice(invoiceNumber);
    }

    @GetMapping
    @Operation(summary = "List all invoices")
    public List<InvoiceResponse> listInvoices() {
        return invoiceService.listAllInvoices();
    }
}
