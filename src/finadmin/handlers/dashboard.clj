(ns finadmin.handlers.dashboard 
  (:require
   [finadmin.database.accountdb :refer [get-account-info]]
   [finadmin.database.transactiondb :as transactiondb]
   [finadmin.views.dashboardview :as dashviews]
   [finadmin.views.transactionsview :as transviews]))


(defn get-all-data
  "Retrieves all data related to the user, including their expense and invoice transactions.
       
       Parameters:
       - `email`: The email of the user whose data is being fetched.
       
       Returns:
       - A map containing the user's expenses and invoices 
         under the keys `:expdata` and `:invdata`, respectively."
  [email]
  (let [expenses (transactiondb/get-expense-transactions-by-email email)
        invoices (transactiondb/get-invoice-transactions-by-email email)]
  {:expdata expenses :invdata invoices}))


(defn dashboard-page
  "Handles the rendering of the dashboard page for an authenticated user.
       
       Parameters:
       - `request`: A map representing the HTTP request, including session data.
       
       Returns:
       - A response map containing the dashboard page if the user is authenticated.
       - A redirect response to the homepage if the user is not authenticated."
  [request]
  (if (nil? request)
    {:status 301
     :headers {"HX-Redirect" "/"}
     :session nil}
    (let [{:keys [email]} (:session request)]
      (if (some? email)
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (dashviews/dashboard email (get-all-data email))}
        {:status 302
         :headers {"Location" "/"}
         :session nil}))))


(defn overview
   "Handles the rendering of the overview page, which shows a summary of the user's transactions.
       
       Parameters:
       - `request`: A map representing the HTTP request, including session data.
       
       Returns:
       - A response map containing the overview page if the user is authenticated."
  [request]
  (let [{:keys [email]} (:session request)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (dashviews/overview-component (get-all-data email)))}))


(defn forms
  "Handles the rendering of the forms page, which displays the various forms available to the user.
       
       Returns:
       - A response map containing the forms page."
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (dashviews/forms-component))})


(defn transactions
  "Handles the rendering of the transactions page, displaying the user's transaction history.
       
       Parameters:
       - `request`: A map representing the HTTP request, including session data.
       
       Returns:
       - A response map containing the transactions page with the user's transaction data."
  [request]
  (let [{:keys [email]} (:session request)
        transactions (transactiondb/get-transactions-by-email email "all")]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (transviews/transactions-component {:transdata transactions}))}))


(defn expenses
  "Handles the rendering of the expenses page, which shows the user's expense transactions.
       
       Returns:
       - A response map containing the expenses page."
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (transviews/expenses-component {}))})


(defn invoices
  "Handles the rendering of the invoices page, which shows the user's invoice transactions.
       
       Returns:
       - A response map containing the invoices page."
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (transviews/invoices-component {}))})


(defn settings
  "Handles the rendering of the settings page, which allows the user to view and edit their account settings.
       
       Parameters:
       - `request`: A map representing the HTTP request, including session data.
       
       Returns:
       - A response map containing the settings page if the user is authenticated."
  [request]
  (let [{:keys [email password]} (:session request)
        account (get-account-info email password)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (dashviews/settings-component account {}))}))


(defn support
  "Handles the rendering of the support page, which provides the user with support options.
       
       Parameters:
       - `_`: This function does not use the request parameter.
       
       Returns:
       - A response map containing the support page."
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (dashviews/support-component))})


(defn logout
  "Handles the logout process, clearing the session and redirecting the user to the homepage.
       
       Returns:
       - A redirect response to the homepage and clears the session."
  [_]
  {:status 301
   :headers {"HX-Redirect" "/"}
   :session nil})