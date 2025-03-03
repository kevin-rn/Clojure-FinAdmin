(ns finadmin.handlers
  (:require
   [finadmin.db :refer [create-user! email-exists? verify-account?]]
   [finadmin.views :as views]))

(defn login-page [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (views/login)})

  ;; Sign in and up handlers
(defn sign-in-page [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (views/sign-in))})

(defn sign-up-page [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (views/sign-up))})

(defn sign-in-account [request]
  (let [{:keys [email password]} (get-in request [:params])]
    (if (verify-account? email password) 
      {:status 301
       :headers {"HX-Redirect" "/dashboard"}
       :session (assoc (:session request) :email email)}
      {:status 404
       :headers {"Content-Type" "text/html"}
       :body (str (views/sign-in-error "Email not registered!"))})))


(defn sign-up-account [request]
  (let [{:keys [email password repeat-password]} (get-in request [:params])]
    (cond
      (not= password repeat-password)
      {:status 404
       :headers {"Content-Type" "text/html"}
       :body (views/sign-up-error "Passwords do not match!")}

      (email-exists? email)
      {:status 404
       :headers {"Content-Type" "text/html"}
       :body (views/sign-up-error "Email already exists!")}

      :else
      (do
        (create-user! email password)
        {:status 301
         :headers {"HX-Redirect" "/dashboard"}
         :session (assoc (:session request) :email email)}))))


(defn dashboard-page [request]
  (let [{:keys [email]} (get-in request [:session])] 
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (views/dashboard email)}))

(defn logout-page [_]
  {:status 301
   :headers {"HX-Redirect" "/"}
   :session nil})



