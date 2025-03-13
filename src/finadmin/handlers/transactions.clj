(ns finadmin.handlers.transactions
  (:require
   [finadmin.database.transactiondb :refer [add-expense-db add-invoice-db
                                            delete-transaction-db
                                            get-transaction-by-id
                                            get-transactions-by-email
                                            modify-transaction-db]]
   [finadmin.views.transactionsview :as views]))


(defn filter-transactions
  [request]
  (let [{:keys [transaction-type]} (:params request)
        {:keys [email]} (:session request)
        transactions (get-transactions-by-email email transaction-type)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (views/transactions-list transactions {}))}))

(defn transaction-details
  [request]
  (let [{:keys [transaction-id transaction-type]} (:path-params request)
        transaction (get-transaction-by-id transaction-id transaction-type)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (views/transaction-details transaction))}))

(defn add-expense
  [request]
  (let [{:keys [email]} (get-in request [:session])
        expense-details (get-in request [:params])]
    (add-expense-db email expense-details)
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (views/expenses-component {:modal true}))}))

(defn add-invoice
  [request]
  (let [{:keys [email]} (:session request)
        invoice-details (:params request)]
    (add-invoice-db email invoice-details)
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (views/invoices-component {:modal true}))}))

(defn modify-transaction
  [request]
  (let [{:keys [transaction-id transaction-type]} (:path-params request)
        updates (get-in request [:params])]
    (modify-transaction-db transaction-id transaction-type updates)
    (let [transaction (get-transaction-by-id transaction-id transaction-type)]
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (views/transaction-details transaction))})))

(defn delete-transaction
  [request]
  (let [{:keys [transaction-id transaction-type]} (:path-params request)
        {:keys [email]} (:session request)
        response (delete-transaction-db transaction-id transaction-type)
        transactions (get-transactions-by-email email "all")]
    (if (:success response)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (views/transactions-component transactions {:modal true}))}
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (views/transactions-component transactions {}))})))