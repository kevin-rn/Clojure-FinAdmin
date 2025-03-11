(ns finadmin.db
  (:require
   [buddy.hashers :as hashers]
   [hikari-cp.core :as cp]
   [next.jdbc :as jdbc]))

(def datasource-options {:auto-commit        true
                         :read-only          false
                         :connection-timeout 30000
                         :validation-timeout 5000
                         :idle-timeout       600000
                         :max-lifetime       1800000
                         :minimum-idle       10
                         :maximum-pool-size  10
                         :pool-name          "db-pool"
                         :adapter            "postgresql"
                         :username           "postgres"
                         :password           "postgres"
                         :database-name      "finadmin"
                         :server-name        "localhost"
                         :port-number        5432
                         :register-mbeans    false})

(defonce datasource
  (delay (cp/make-datasource datasource-options)))

(def database-connection {:datasource @datasource})

(def insert-transaction-query
  "INSERT INTO transactions (amount, currency, description, account_email, transaction_type, payment_method) 
   VALUES (?, ?, ?, ?, ?, ?) 
   RETURNING transaction_id;")

(def insert-invoice-query 
  "INSERT INTO invoices (transaction_id, invoice_number, vendor_name, po_number, vat_code, payment_terms, due_date, payment_status) 
   VALUES (?, ?, ?, ?, ?, ?, ?, ?)")

(def insert-expense-query
  "INSERT INTO expenses (transaction_id, expense_type, reimbursement_status, business_purpose, approval_status, expense_date) 
   VALUES (?, ?, ?, ?, ?, ?)")


(defn create-user!
  "Creates a user account in the database with a secure hashed password.

   Parameters:
   - email: A string representing the user's email address.
   - password: A string representing the user's chosen password.
   
   Returns:
   - false if the email is nil; otherwise, returns nil after executing the database query to create the account."
  [email password]
  (if (nil? email)
    false
    (let [hashed-password (hashers/derive password {:alg :pbkdf2+sha256})]
      (jdbc/execute-one! database-connection
                         ["INSERT INTO accounts (email, password) VALUES (?, ?)" email hashed-password]))))

(defn email-exists?
  "Checks if the provided email exists in the database.

   Parameters:
   - email: A string representing the user's email address.
   
   Returns:
   - boolean indicating true if the email matches an existing account, false otherwise."
  [email]
  (if (nil? email)
    false
    (let [result (jdbc/execute! database-connection ["SELECT COUNT(1) FROM accounts WHERE email = ?" email])]
      (> (get (first result) :count) 0))))

(defn verify-account?
  "Verifies if the given email and password match an existing account.

   Parameters:
   - email: A string representing the user's email address.
   - password: A string representing the user's entered password.
   
   Returns:
   - true if the email exists and the provided password matches the stored hashed password; 
   - false if the email doesn't exist or the password is incorrect."
  [email password]
  (let [query "SELECT password FROM accounts WHERE email = ?"
        result (jdbc/execute-one! database-connection [query email])]
    (when-let [stored-hash (:accounts/password result)]
      (:valid (hashers/verify password stored-hash {:alg :pbkdf2+sha256})))))


(defn add-invoice-db
  "Adds a new invoice entry to the database, including transaction and invoice details.
  
     Parameters:
     - email: A string representing the email of the account creating the invoice.
     - invoice-details: A map containing the details of the invoice."
  [email invoice-details] 
  ((jdbc/with-transaction [dc database-connection]
     (let [transaction-result (jdbc/execute! dc [insert-transaction-query
                                                 (invoice-details :amount)
                                                 (invoice-details :currency)
                                                 (invoice-details :description)
                                                 email
                                                 "invoice"
                                                 (invoice-details :payment_method)])
           transaction-id (get-in transaction-result [0 :transactions/transaction_id])]
       (jdbc/execute-one! dc [insert-invoice-query
                              transaction-id
                              (invoice-details :invoice_number)
                              (invoice-details :vendor_name)
                              (invoice-details :po_number)
                              (invoice-details :vat_code)
                              (invoice-details :payment_terms)
                              (invoice-details :due_date)
                              (invoice-details :payment_status)])))))


(defn add-expense-db
  "Adds a new expense entry to the database, including transaction and expense details.

   Parameters:
   - email: A string representing the email of the account creating the expense.
   - expense-details: A map containing the details of the expens."
  [email expense-details]
  (jdbc/with-transaction [dc database-connection]
    (let [transaction-result (jdbc/execute! dc
                                            [insert-transaction-query
                                             (:amount expense-details)
                                             (:currency expense-details)
                                             (:description expense-details)
                                             email
                                             "expense"
                                             (:payment_method expense-details)])
          transaction-id (get-in transaction-result [0 :transactions/transaction_id])]
      (jdbc/execute! dc
                     [insert-expense-query
                      transaction-id
                      (:expense_type expense-details)
                      (:reimbursement_status expense-details)
                      (:business_purpose expense-details)
                      (:approval_status expense-details)
                      (:expense_date expense-details)]))))

(defn build-update-query
  "Generates an UPDATE SQL query and corresponding values for fields that are not nil.
  
     Parameters:
     - table-name: A string representing the name of the table to update (e.g., 'invoices' or 'expenses').
     - id-field: A string representing the name of the ID field for the table (e.g., 'invoice_id' or 'expense_id').
     - updates: A map containing the fields and their new values to update in the table.
     
     Returns:
     - A vector containing the SQL query string and its corresponding values. If no updates are found, returns nil."
  [table-name id-field updates]
  (let [fields (->> updates
                    (remove #(nil? (second %)))
                    (map #(str (name (first %)) " = ?"))
                    (interpose ", ")
                    (apply str))
        values (->> updates
                    (filterv some?)
                    (map second)
                    (vec))]
    (when (not-empty fields)
      [(str "UPDATE " table-name " SET " fields " WHERE " id-field " = ?")
       (into values [(:id updates)])])))


(defn modify-invoice
  "Modifies an existing invoice in the database.
  
     Parameters:
     - invoice-id: A unique identifier for the invoice to update.
     - updates: A map containing the fields to update and their new values."
  [invoice-id updates]
  (let [updates-with-id (assoc updates :id invoice-id)
        [query params] (build-update-query "invoices" "invoice_id" updates-with-id)]
    (when query
      (jdbc/execute-one! database-connection [query params]))))


(defn modify-expense
  "Modifies an existing expense in the database.
  
     Parameters:
     - expense-id: A unique identifier for the expense to update.
     - updates: A map containing the fields to update and their new values."
  [expense-id updates]
  (let [updates-with-id (assoc updates :id expense-id)
        [query params] (build-update-query "expenses" "expense_id" updates-with-id)]
    (when query
      (jdbc/execute-one! database-connection [query params]))))


(defn delete-invoice
  "Deletes an invoice and its associated transaction from the database.
  
     Parameters:
     - invoice-id: A unique identifier for the invoice to delete."
  [invoice-id]
  (jdbc/with-transaction [dc database-connection]
    (let [transaction-id (:transaction_id (jdbc/execute-one! dc ["SELECT transaction_id FROM invoices WHERE invoice_id = ?" invoice-id]))]
      (jdbc/execute-one! dc ["DELETE FROM invoices WHERE invoice_id = ?" invoice-id])
      (jdbc/execute-one! dc ["DELETE FROM transactions WHERE transaction_id = ?" transaction-id]))))

(defn delete-expense
  "Deletes an expense and its associated transaction from the database.
  
     Parameters:
     - expense-id: A unique identifier for the expense to delete."
  [expense-id]
  (jdbc/with-transaction [dc database-connection]
    (let [transaction-id (:transaction_id (jdbc/execute-one! dc ["SELECT transaction_id FROM expenses WHERE expense_id = ?" expense-id]))]
      (jdbc/execute-one! dc ["DELETE FROM expenses WHERE expense_id = ?" expense-id])
      (jdbc/execute-one! dc ["DELETE FROM transactions WHERE transaction_id = ?" transaction-id]))))


(defn delete-account
  "Deletes a user account and all associated transactions (on cascade also invoices and expenses).
   
   Parameters:
   - email: A string representing the email of the account to delete."
  [email]
  (jdbc/with-transaction [dc database-connection]
    (jdbc/execute-one! dc ["DELETE FROM accounts WHERE email = ?", email])
    (jdbc/execute-one! dc ["DELETE FROM transactions WHERE account_email = ?", email])))

(defn get-transactions-by-email
  "Retrieves transactions for a specific user based on their email and optional transaction type.

   Parameters:
   - email: A string representing the user's email address.
   - transaction-type: A string representing the type of transaction (optional).
   
   Returns:
   - A sequence of maps, where each map represents a transaction."
  [email transaction-type]
  (let [query (cond
                (= transaction-type "all")  ["SELECT * FROM transactions WHERE account_email = ?" email]
                (some #{"invoice" "expense"} [transaction-type]) ["SELECT * FROM transactions WHERE account_email = ? AND transaction_type = ?" email transaction-type]
                :else (throw (ex-info "Invalid transaction type" {:type transaction-type})))]
    (jdbc/execute! database-connection query)))
