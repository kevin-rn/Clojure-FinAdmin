(ns finadmin.handlers.transactions 
  (:require
   [finadmin.database.transactiondb :refer [add-expense-db add-invoice-db
                                            get-transactions-by-email]]
   [finadmin.views.transactionsview :as views]))


(defn filter-transactions
  [request]
  (let [{:keys [transaction-type]} (get-in request [:params])
        {:keys [email]} (get-in request [:session])
        transactions (get-transactions-by-email email transaction-type)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (views/transactions-list transactions))}))

(defn add-expense
  [request]
  (let [{:keys [email]} (get-in request [:session])
        expense-details (request :params)]
    (add-expense-db email expense-details)
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (views/expenses-component {:modal true}))}))

(defn add-invoice
  [request]
  (let [{:keys [email]} (get-in request [:session])
        invoice-details (request :params)]
    (add-invoice-db email invoice-details)
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (views/invoices-component {:modal true}))}))
