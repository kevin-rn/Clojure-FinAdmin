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
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,  -- (e.g., 'USD', 'EUR')
    description TEXT,
    account_email TEXT NOT NULL,  -- Accounts email
    payment_status VARCHAR(20) DEFAULT 'pending',  -- (e.g., 'paid', 'pending')
    transaction_type VARCHAR(20) NOT NULL,  --  ('invoice' or 'expense')
    FOREIGN KEY (account_email) REFERENCES accounts(email)
);

-- Table to store invoices
CREATE TABLE invoices
(
    invoice_id SERIAL PRIMARY KEY,
    transaction_id INT NOT NULL,
    invoice_number VARCHAR(50) NOT NULL,
    due_date TIMESTAMP NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,  -- (e.g., 'USD', 'EUR')
    status VARCHAR(20) DEFAULT 'unpaid',  -- (e.g., 'paid', 'unpaid')
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id)
);

-- Table to store expenses
CREATE TABLE expenses
(
    expense_id SERIAL PRIMARY KEY,
    transaction_id INT NOT NULL,
    expense_category VARCHAR(50) NOT NULL,  -- (e.g., 'office supplies', 'travel')
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,  -- (e.g., 'USD', 'EUR')
    description TEXT,
    status VARCHAR(20) DEFAULT 'pending',  -- (e.g., 'approved', 'pending')
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id)
);
