(ns finadmin.handlers.dashboard 
  (:require
   [finadmin.database.accountdb :refer [get-account-info]]
   [finadmin.database.transactiondb :refer [get-transactions-by-email]]
   [finadmin.views.dashboardview :as dashviews]
   [finadmin.views.transactionsview :as transviews]))


(defn dashboard-page
  [request]
  (if (nil? request)
    {:status 301
     :headers {"HX-Redirect" "/"}
     :session nil}
    (let [{:keys [email]} (:session request)]
      (if (some? email)
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (dashviews/dashboard email (get-transactions-by-email email "all"))}
        {:status 302
         :headers {"Location" "/"}
         :session nil}))))

(defn overview
  [request]
  (let [{:keys [email]} (:session request)
        transactions (get-transactions-by-email email "all")]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (dashviews/overview-component transactions))}))

(defn forms
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (dashviews/forms-component))})

(defn transactions
  [request]
  (let [{:keys [email]} (:session request)
        transactions (get-transactions-by-email email "all")]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (transviews/transactions-component transactions {}))}))

(defn expenses
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (transviews/expenses-component {}))})

(defn invoices
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (transviews/invoices-component {}))})

(defn settings
  [request]
  (let [{:keys [email password]} (:session request)
        account (get-account-info email password)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (dashviews/settings-component account {}))}))

(defn support
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (dashviews/support-component))})

(defn logout
  [_]
  {:status 301
   :headers {"HX-Redirect" "/"}
   :session nil})