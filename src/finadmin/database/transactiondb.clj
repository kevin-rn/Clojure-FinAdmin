(ns finadmin.database.transactiondb
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
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

;; Used for filtering columns in update query
  (def transaction-columns
    #{:amount :currency :description :payment_method})

  (def expense-columns
    #{:expense_type :reimbursement_status :business_purpose :approval_status :expense_date})

  (def invoice-columns
    #{:invoice_number :vendor_name :po_number :vat_code :payment_terms :due_date :payment_status})

  (defn filter-valid-keys [updates valid-columns]
    (select-keys updates valid-columns))

  (defn parse-value [k v]
    (cond
      (#{:due_date :expense_date} k) (java.time.LocalDate/parse v)
      (= :amount k) (Double/parseDouble (str v))
      :else v))

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
    [table-name updates transaction-id]
    (let [filtered-updates (filterv #(some? (second %)) updates)
          transaction-id (Integer/parseInt transaction-id)]
      (when (seq filtered-updates)
        (let [columns (map (comp name first) filtered-updates)
              values (mapv (fn [[k v]] (parse-value k v)) filtered-updates)
              set-clause (str/join ", " (map #(str % " = ?") columns))]
          [(str "UPDATE " table-name " SET " set-clause " WHERE transaction_id = ?")
           (conj values transaction-id)]))))


  (defn modify-transaction-db
    "Modifies an existing record in the database.
  
     Parameters:
     - transaction-id: A unique identifier for the specific transaction record to modify.
     - transaction-type: The type of transaction to modify, either 'expense' or 'invoice'.
     - updates: A map containing the fields to update and their new values."
    [transaction-id transaction-type updates]
    (jdbc/with-transaction [dc database-connection]
      (let [table (case transaction-type
                    "expense" "expenses"
                    "invoice" "invoices")
            valid-columns (case transaction-type
                            "expense" expense-columns
                            "invoice" invoice-columns)
            filtered-updates (filter-valid-keys updates (set/union transaction-columns valid-columns))
            {transaction-updates true table-updates false} (group-by #(contains? transaction-columns (key %)) filtered-updates)
            [tx-query tx-params] (build-update-query "transactions" transaction-updates transaction-id)
            [sub-query sub-params] (build-update-query table table-updates transaction-id)]
        (jdbc/execute! dc (into [tx-query] tx-params))
        (jdbc/execute! dc (into [sub-query] sub-params)))))

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