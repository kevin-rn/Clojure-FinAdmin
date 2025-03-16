(ns finadmin.handlers.transactions
  (:require
   [finadmin.database.transactiondb :as transactiondb]
   [finadmin.views.transactionsview :as views]))


(defn filter-transactions
  "Filters transactions based on the given transaction type (e.g., expenses, invoices) 
   and returns a list of matching transactions for the authenticated user.
       
       Parameters:
       - `request`: A map representing the HTTP request, which includes the transaction type 
         in `:params` and the user's email in the session.
       
       Returns:
       - A response map containing the filtered transactions list for the user."
  [request]
  (let [{:keys [transaction-type]} (:params request)
        {:keys [email]} (:session request)
        transactions (transactiondb/get-transactions-by-email email transaction-type)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (views/transactions-list {:transdata transactions}))}))


(defn transaction-details
   "Fetches and displays the details of a specific transaction based on its ID and type.
       
       Parameters:
       - `request`: A map representing the HTTP request, which includes the transaction ID and type in `:path-params`.
       
       Returns:
       - A response map containing the details of the requested transaction."
  [request]
  (let [{:keys [transaction-id transaction-type]} (:path-params request)
        transaction (transactiondb/get-transaction-by-id transaction-id transaction-type)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (views/transaction-details {:transaction transaction}))}))


(defn add-expense
  "Adds a new expense transaction for the authenticated user and shows a success message.
       
       Parameters:
       - `request`: A map representing the HTTP request, which includes the user's email 
         in the session and the expense details in `:params`.
       
       Returns:
       - A response map containing the updated expenses page and a success message."
  [request]
  (let [{:keys [email]} (get-in request [:session])
        expense-details (get-in request [:params])
        modal_message "Expense has been registered succesfully!"]
    (transactiondb/add-expense-db email expense-details)
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (views/expenses-component {:modal modal_message}))}))


(defn add-invoice
  "Adds a new invoice transaction for the authenticated user and shows a success message.
       
       Parameters:
       - `request`: A map representing the HTTP request, which includes the user's email
         in the session and the invoice details in `:params`.
       
       Returns:
       - A response map containing the updated invoices page and a success message."
  [request]
  (let [{:keys [email]} (:session request)
        invoice-details (:params request)
        modal_message "Invoice has been registered succesfully!"]
    (transactiondb/add-invoice-db email invoice-details)
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (views/invoices-component {:modal modal_message}))}))


(defn modify-transaction
  "Modifies the details of an existing transaction and shows a success message.
       
       Parameters:
       - `request`: A map representing the HTTP request, which includes the transaction ID 
         and type in `:path-params` and the updated details in `:params`.
       
       Returns:
       - A response map containing the updated transaction details and a success message."
  [request]
  (let [{:keys [transaction-id transaction-type]} (:path-params request)
        updates (get-in request [:params])
        modal_message "Transaction has been succesfully updated!"]
    (transactiondb/modify-transaction-db transaction-id transaction-type updates)
    (let [transaction (transactiondb/get-transaction-by-id transaction-id transaction-type)]
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (views/transaction-details {:transaction transaction 
                                              :modal modal_message}))})))


(defn delete-transaction
  "Deletes a transaction based on the given ID and type, and updates the list of transactions.
       
       Parameters:
       - `request`: A map representing the HTTP request, which includes the transaction ID 
         and type in `:path-params` and the user's email in the session.
       
       Returns:
       - A response map containing the updated transactions list and a success message 
         if the deletion was successful, or the original list if not."
  [request]
  (let [{:keys [transaction-id transaction-type]} (:path-params request)
        {:keys [email]} (:session request)
        response (transactiondb/delete-transaction-db transaction-id transaction-type)
        transactions (transactiondb/get-transactions-by-email email "all")
        modal_message "Transaction has been deleted succesfully!"]
    (if (:success response)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (views/transactions-component {:transdata transactions 
                                                 :modal modal_message}))}
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (views/transactions-component {:transdata transactions}))})))