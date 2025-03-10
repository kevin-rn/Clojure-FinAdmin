(ns finadmin.handlers
  (:require
   [finadmin.db :refer [create-user! email-exists? verify-account?]]
   [finadmin.views :as views]))

(defn login-page 
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (views/login)})

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
       :session (assoc (:session request) :email email)})))



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
         :session (assoc (:session request) :email email)}))))

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
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (views/transactions-component))})

(defn invoices
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (views/invoices-component))})

(defn expenses
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (views/expenses-component))})

(defn settings
  [request]
  (let [{:keys [email]} (get-in request [:session])]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (views/settings-component email))}))


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



