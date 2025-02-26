(ns finadmin.handlers
    (:require
     [finadmin.views :as views]))
 
  ;; Sign in and up handlers
  (defn sign-in-page [_]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (views/login "sign-in")})
  
  (defn sign-up-page [_]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (views/login "sign-up")})
  
  (defn dashboard-page [_]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (views/dashboard)})