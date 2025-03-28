-- Table to store user accounts
CREATE TABLE accounts
(
    email TEXT PRIMARY KEY,
    password TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT current_timestamp
);

-- Table to store unified transactions (for both invoices and expenses)
CREATE TABLE transactions
(
    transaction_id SERIAL PRIMARY KEY,
    transaction_date TIMESTAMP NOT NULL DEFAULT current_timestamp,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount >= 0),
    currency VARCHAR(3) NOT NULL CHECK (char_length(currency) = 3),  -- ISO currency code
    description TEXT,
    account_email TEXT NOT NULL,  -- Accounts email
    transaction_type VARCHAR(20) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    FOREIGN KEY (account_email) REFERENCES accounts(email) ON DELETE CASCADE
);

-- Table to store expense-specific fields
CREATE TABLE expenses
(
    expense_id SERIAL PRIMARY KEY,
    transaction_id INT NOT NULL UNIQUE,
    expense_type VARCHAR(50) NOT NULL,  -- (e.g., Travel, Meals, Office Supplies, etc.)
    reimbursement_status VARCHAR(20),
    business_purpose TEXT NOT NULL,  -- Justification for the expense
    approval_status VARCHAR(20) NOT NULL,
    expense_date TIMESTAMP NOT NULL DEFAULT current_timestamp,  -- Date the expense was incurred
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

-- Table to store invoice specific fields
CREATE TABLE invoices
(
    invoice_id SERIAL PRIMARY KEY,
    transaction_id INT NOT NULL,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    vendor_name TEXT NOT NULL,
    po_number VARCHAR(50),
    vat_code VARCHAR(20),  -- VAT Code (e.g., "21%")
    payment_terms VARCHAR(50) NOT NULL,  -- Payment conditions (e.g., 'Net 30', 'net 60')
    due_date TIMESTAMP NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

