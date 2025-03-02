(ns finadmin.handlers
    (:require
     [finadmin.db :refer [create-user! email-exists?]]
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

  (defn sign-up-account [request]
    (let [{:keys [email password repeat-password]} (get-in request [:params])]
      (cond
        (not= password repeat-password)  ;; Passwords do not match
        {:status 400
         :headers {"Content-Type" "text/html"}
         :body (views/sign-up-error "Passwords do not match!")}
    
        (email-exists? email)
        {:status 400
         :headers {"Content-Type" "text/html"}
         :body (views/sign-up-error "Email already exists!")}
    
        :else
        (do
          (create-user! email password)
          {:status 200
           :headers {"Content-Type" "text/html"}
           :body (views/dashboard)}))))


  (defn dashboard-page [_]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (views/dashboard)})
  
  ;; (defn logout-page [request]
  ;;   {:status 200
  ;;    :headers {"Content-Type" "text/html"}
  ;;    :session {}
  ;;    :body "You have been logged out"})
