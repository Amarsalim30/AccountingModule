-- Insert required accounts for the Accounting Module

-- Accounts Receivable (Asset)
INSERT INTO account (type, code, name) VALUES ('ASSET', 'AR', 'Accounts Receivable');

-- Cash Account (Asset) - optional example
INSERT INTO account (type, code, name) VALUES ('ASSET', 'CASH', 'Cash');

-- Sales Revenue (Revenue)
INSERT INTO account (type, code, name) VALUES ('REVENUE', 'REV', 'Sales Revenue');

-- VAT Payable (Liability)
INSERT INTO account (type, code, name) VALUES ('LIABILITY', 'VAT', 'VAT Payable');

-- Additional Example: General Expenses (Expense)
INSERT INTO account (type, code, name) VALUES ('EXPENSE', 'EXP', 'General Expenses');

-- Additional Example: Owner's Equity (Equity)
INSERT INTO account (type, code, name) VALUES ('EQUITY', 'EQ', 'Owner''s Equity');
