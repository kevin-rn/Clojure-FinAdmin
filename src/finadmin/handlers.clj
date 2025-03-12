(ns finadmin.handlers
  (:require
   [finadmin.db :refer [add-expense-db add-invoice-db create-user!
                        delete-account-db email-exists? get-account-info
                        get-transactions-by-email update-password-db
                        verify-account?]]
   [finadmin.views :as views]))

(defn login-page
  [request]
  (let [{:keys [email]} (get-in request [:session])]
    (if (nil? email)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (views/login)}
      {:status 302
       :headers {"Location" "/dashboard"}})))

(defn sign-in-page
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (views/sign-in {}))})

(defn sign-up-page
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (views/sign-up {}))})

(defn sign-in-account
  [request]
  (let [{:keys [email password]} (get-in request [:params])]
    (cond
      (or (empty? email) (empty? password))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (views/sign-in {:error "All fields are required!"}))}

      (not (verify-account? email password))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (views/sign-in {:error "Invalid email or password!"}))}

      :else
      {:status 301
       :headers {"HX-Redirect" "/dashboard"}
       :session (assoc (:session request) :email email :password password)})))



(defn sign-up-account
  [request]
  (let [{:keys [email password repeat-password]} (get-in request [:params])]
    (cond
      (or (empty? email) (empty? password) (empty? repeat-password))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (views/sign-up {:error "All fields are required!"}))}

      (not= password repeat-password)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (views/sign-up {:error "Passwords do not match!"}))}

      (email-exists? email)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (views/sign-up {:error "Email already exists!"}))}

      :else
      (do
        (create-user! email password)
        {:status 301
         :headers {"HX-Redirect" "/dashboard"}
         :session (assoc (:session request) :email email :password password)}))))

;;__________________________________________________

(defn dashboard-page
  [request]
  (if (nil? request)
    {:status 301
     :headers {"HX-Redirect" "/"}
     :session nil}
    (let [{:keys [email]} (get-in request [:session])]
      (if (some? email)
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (views/dashboard email)}
        {:status 302
         :headers {"Location" "/"}
         :session nil}))))

(defn overview
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (views/overview-component))})

(defn forms
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (views/forms-component))})

(defn transactions
  [request]
  (let [{:keys [email]} (get-in request [:session])
        transactions (get-transactions-by-email email "all")]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (views/transactions-component transactions))}))

(defn invoices
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (views/invoices-component {}))})

(defn expenses
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (views/expenses-component {}))})

(defn settings
  [request]
  (let [{:keys [email password]} (get-in request [:session])
        account (get-account-info email password)] 
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (views/settings-component account {}))}))

(defn support
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (views/support-component))})

(defn logout
  [_]
  {:status 301
   :headers {"HX-Redirect" "/"}
   :session nil})


(defn filter-transactions 
  [request]
  (let [{:keys [transaction-type]} (get-in request [:params])
        {:keys [email]} (get-in request [:session])
        transactions (get-transactions-by-email email transaction-type)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str (views/transactions-list transactions))}
    ))

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

(defn update-password
  [request]
  (let [{:keys [current-password new-password verify-password]} (get-in request [:params])
        {:keys [email]} (get-in request [:session])
        current-account (get-account-info email current-password)]
     (cond
       (or (empty? current-password) (empty? new-password) (empty? verify-password))
       {:status 200
        :headers {"Content-Type" "text/html"}
        :body (str (views/settings-component current-account {:error "All fields are required!"}))}
  
       (not= new-password verify-password)
       {:status 200
        :headers {"Content-Type" "text/html"}
        :body (str (views/settings-component current-account {:error "Passwords do not match!"}))}
  
       :else
       (do
         (update-password-db email new-password)
         (let [new-account (get-account-info email new-password)]
         {:status 200
          :headers {"Content-Type" "text/html"}
          :body (str (views/settings-component new-account {:modal true}))
          :session (assoc (:session request) :password new-password)})
         ))))

(defn delete-account
  [request]
  (let [{:keys [email]} (get-in request [:session])]
    (delete-account-db email))
  {:status 301
   :headers {"HX-Redirect" "/"}
   :session nil}
  )