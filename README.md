# Basic Financial Administration system built using Clojure


## Project Structure

clojure-finance/  
│── deps.edn 					# Project dependencies  
│── resources/  
│── src/  
│ │─── clojure_finance/  
│ │ │─── core.clj 					# Entry point (Ring)  
│ │ │─── routes.clj 					# API and HTML routes (Reitit)  
│ │ │─── handlers.clj 				# Request handlers  
│ │ │─── storage.clj 					# In-memory DB using atom  
│ │ │─── views.clj 					# HTML rendering (Hiccup/HTMX)  
│── README.md # Project documentation  

## Tech Stack
Backend: Clojure (Ring, Reitit)
Frontend: HTMX (Minimal JavaScript, dynamic updates via AJAX)
Data Storage: In-memory DB (using Clojure atom)

## Features
- Accounts
    - User login (basic session handling)
    - Simple password-based authentication (plaintext for simplicity, but hashing recommended)

- Financial Transactions
    - Add transaction (amount, description, date)
    - Edit transaction
    - Delete transaction
    - List transactions (sorted by date)
    - Basic account balance calculation



