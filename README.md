Accounting Module for POS System
A comprehensive Accounting Module for a Point-of-Sale (POS) system built with Spring Boot. This module implements double-entry bookkeeping and covers core accounting functionalities such as invoice processing, payment recording, expense tracking, and more. It is designed to be robust, scalable, and production-ready, supporting both small businesses and larger enterprises.

Overview
This repository contains a Spring Boot microservice that manages accounting transactions for a POS system. It demonstrates how to:

Create invoices that automatically generate balanced journal entries.
Process customer payments against outstanding invoices.
Record operational expenses.
(Optionally) Integrate with inventory management, purchase orders, and supplier payments.
Generate essential financial reports such as Income Statements, Balance Sheets, and Cash Flow Statements.
The module adheres to the principles of double-entry accounting by ensuring every transaction (journal entry) has equal debits and credits.

Features
Core Accounting Functionalities
Invoice Processing:
Accepts invoice details (invoice number, subtotal, tax rate).
Calculates VAT and total invoice amount.
Generates journal entries:
Debit Accounts Receivable for the total amount.
Credit Sales Revenue and VAT Payable accordingly.
Payment Processing:
Records customer payments, updating invoice status and creating corresponding journal entries.
Expense Tracking:
Logs operational expenses (e.g., rent, utilities) and generates the appropriate accounting entries.
Purchase Orders & Accounts Payable:
Manages supplier transactions for credit purchases and processes supplier payments.
Financial Reporting:
Provides endpoints to generate Income Statements, Balance Sheets, and Cash Flow Statements.
(Optional) Multi-Currency Support:
Records transactions in different currencies and converts amounts for consolidated reporting.
Audit Trails:
Logs every transaction with timestamps and user identifiers for transparency and compliance.
