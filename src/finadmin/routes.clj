(ns finadmin.routes
  (:require
   [finadmin.handlers :as handlers]
   [reitit.ring :as ring]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [ring.middleware.session :refer [wrap-session]]))

(def app
  (-> (ring/ring-handler
       (ring/router
        [;; Login Page
         ["/" {:get handlers/login-page}]
         ["/sign-in" {:get handlers/sign-in-page :post handlers/sign-in-account}]
         ["/sign-up" {:get handlers/sign-up-page :post handlers/sign-up-account}]

         ;; Dashboard links
         ["/dashboard" {:get handlers/dashboard-page}]
         ["/overview" {:get handlers/overview}]
         ["/forms" {:get handlers/forms}]
         ["/transactions" {:get handlers/transactions}]
         ["/invoices" {:get handlers/invoices}]
         ["/expenses" {:get handlers/expenses}]
         ["/support" {:get handlers/support}]
         ["/settings" {:get handlers/settings}]
         ["/logout" {:get handlers/logout}]

         ;; Interaction
         ["/filter-transactions" {:get handlers/filter-transactions}]
         ["/add-expense" {:post handlers/add-expense}]
         ["/add-invoice" {:post handlers/add-invoice}]
         ["/update-password" {:post handlers/update-password}]
         ["/delete-account" {:post handlers/delete-account}]]
         ))
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      (wrap-session)))
