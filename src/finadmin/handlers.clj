(ns finadmin.handlers
    (:require
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

  (defn dashboard-page [_]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (views/dashboard)})