(ns finadmin.database.transactiondb
  (:require
   [finadmin.database.datasource :refer [database-connection]]
   [next.jdbc :as jdbc]))


;; Queries
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

(def get-expense-transaction-query
  "SELECT * FROM transactions t INNER JOIN expenses e ON t.transaction_id = e.transaction_id WHERE t.transaction_id = ?")

(def get-invoice-transaction-query
  "SELECT * FROM transactions t INNER JOIN invoices i ON t.transaction_id = i.transaction_id WHERE t.transaction_id = ?")

;; Transactions
(defn add-expense-db
  "Adds a new expense entry to the database, including transaction and expense details.

   Parameters:
   - email: A string representing the email of the account creating the expense.
   - expense-details: A map containing the details of the expens."
  [email expense-details]
  (jdbc/with-transaction [dc database-connection] 
    (let [transaction-result (jdbc/execute! dc
                                            [insert-transaction-query
                                             (Double. (:amount expense-details))
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
                      (java.time.LocalDate/parse (:expense_date expense-details))]))))

(defn add-invoice-db
  "Adds a new invoice entry to the database, including transaction and invoice details.
  
     Parameters:
     - email: A string representing the email of the account creating the invoice.
     - invoice-details: A map containing the details of the invoice."
  [email invoice-details] 
  (jdbc/with-transaction [dc database-connection]
     (let [transaction-result (jdbc/execute! dc [insert-transaction-query
                                                 (Double. (:amount invoice-details))
                                                 (:currency invoice-details)
                                                 (:description invoice-details)
                                                 email
                                                 "invoice"
                                                 (:payment_method invoice-details)])
           transaction-id (get-in transaction-result [0 :transactions/transaction_id])]
       (jdbc/execute-one! dc [insert-invoice-query
                              transaction-id
                              (:invoice_number invoice-details)
                              (:vendor_name invoice-details)
                              (:po_number invoice-details)
                              (:vat_code invoice-details)
                              (:payment_terms invoice-details)
                              (java.time.LocalDate/parse (:due_date invoice-details))
                              (:payment_status invoice-details)]))))

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
                (some #{"invoice" "expense"} [transaction-type])
                ["SELECT * FROM transactions WHERE account_email = ? AND transaction_type = ?" email transaction-type]
                :else (throw (ex-info "Invalid transaction type" {:type transaction-type})))]
    (jdbc/execute! database-connection query)))

(defn get-transaction-by-id
  [transaction-id transaction-type]
  (let [query (case (keyword transaction-type)
                :expense get-expense-transaction-query
                :invoice get-invoice-transaction-query
                (throw (IllegalArgumentException. "Invalid transaction type")))
        transaction-id (Integer. transaction-id)]
    (jdbc/execute-one! database-connection [query transaction-id])))

(defn build-update-query
  "Generates an UPDATE SQL query and corresponding values for fields that are not nil.
  
     Parameters:
     - table-name: A string representing the name of the table to update (e.g., 'invoices' or 'expenses').
     - updates: A map containing the fields and their new values to update in the table.
     
     Returns:
     - A vector containing the SQL query string and its corresponding values. If no updates are found, returns nil."
  [table-name updates]
  (let [valid-updates (->> updates
                           (remove #(nil? (second %)))
                           (map #(str (name (first %)) " = ?"))
                           (interpose ", ")
                           (apply str))
        values (->> updates
                    (filterv (comp some? second))
                    (map second)
                    (vec))]
    (when (seq valid-updates)
      [(str "UPDATE " table-name " SET " valid-updates " WHERE transaction_id = ?")
       (into values [(:id updates)])])))

(defn modify-transaction-db
  "Modifies an existing record in the database.
  
     Parameters:
     - record-id: A unique identifier for the specific transaction record to modify.
     - transaction-type: The type of transaction to modify, either 'expense' or 'invoice'.
     - updates: A map containing the fields to update and their new values."
  [record-id transaction-type updates]
  (jdbc/with-transaction [dc database-connection]
    (let [table (case transaction-type
                  "expense" "expenses"
                  "invoice" "invoices")]
      (if (and table record-id (seq updates))
        (let [[query params] (build-update-query table updates)]
          (jdbc/execute! dc [query params]))
        (throw (ex-info "Invalid transaction type or updates." {:transaction-type transaction-type :updates updates}))))))

(defn delete-transaction-db
  "Deletes a record (expense or invoice) and its associated transaction from the database.
  
     Parameters:
     - :transaction-id: A unique identifier for the specific transaction record to delete.
     - transaction-type: The type of transaction to delete, either 'expense' or 'invoice'.
  
     Returns `{:success true} `if deleted, otherwise `{:success false} `. "
  [transaction-id transaction-type]
  (jdbc/with-transaction [dc database-connection]
    (let [table (case transaction-type
                  "expense" "expenses"
                  "invoice" "invoices")
          transaction-id (Integer/parseInt transaction-id)
          deleted_row_1 (:next.jdbc/update-count (jdbc/execute-one! dc [(str "DELETE FROM " table " WHERE transaction_id = ?") transaction-id]))
          deleted_row_2 (:next.jdbc/update-count (jdbc/execute-one! dc ["DELETE FROM transactions WHERE transaction_id = ?" transaction-id]))]
      {:success (and (pos? deleted_row_1) (pos? deleted_row_2))})))