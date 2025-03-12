(ns finadmin.handlers.auth 
  (:require
   [finadmin.database.accountdb :refer [create-user! delete-account-db
                                        email-exists? get-account-info
                                        update-password-db verify-account?]]
   [finadmin.views.authview :as authviews]
   [finadmin.views.dashboardview :as dashviews]))


(defn login-page
  [request]
  (let [{:keys [email]} (get-in request [:session])]
    (if (nil? email)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (authviews/login)}
      {:status 302
       :headers {"Location" "/dashboard"}})))

(defn sign-in-page
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (authviews/sign-in {}))})

(defn sign-up-page
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (authviews/sign-up {}))})

(defn sign-in-account
  [request]
  (let [{:keys [email password]} (get-in request [:params])]
    (cond
      (or (empty? email) (empty? password))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (authviews/sign-in {:error "All fields are required!"}))}

      (not (verify-account? email password))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (authviews/sign-in {:error "Invalid email or password!"}))}

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
       :body (str (authviews/sign-up {:error "All fields are required!"}))}

      (not= password repeat-password)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (authviews/sign-up {:error "Passwords do not match!"}))}

      (email-exists? email)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (authviews/sign-up {:error "Email already exists!"}))}

      :else
      (do
        (create-user! email password)
        {:status 301
         :headers {"HX-Redirect" "/dashboard"}
         :session (assoc (:session request) :email email :password password)}))))

(defn update-password
  [request]
  (let [{:keys [current-password new-password verify-password]} (get-in request [:params])
        {:keys [email]} (get-in request [:session])
        current-account (get-account-info email current-password)]
    (cond
      (or (empty? current-password) (empty? new-password) (empty? verify-password))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (dashviews/settings-component current-account {:error "All fields are required!"}))}

      (not= new-password verify-password)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (dashviews/settings-component current-account {:error "Passwords do not match!"}))}

      :else
      (do
        (update-password-db email new-password)
        (let [new-account (get-account-info email new-password)]
          {:status 200
           :headers {"Content-Type" "text/html"}
           :body (str (dashviews/settings-component new-account {:modal true}))
           :session (assoc (:session request) :password new-password)})))))

(defn delete-account
  [request]
  (let [{:keys [email]} (get-in request [:session])]
    (delete-account-db email))
  {:status 301
   :headers {"HX-Redirect" "/"}
   :session nil})