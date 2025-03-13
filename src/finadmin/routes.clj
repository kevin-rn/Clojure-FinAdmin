(ns finadmin.routes
  (:require
   [finadmin.handlers.auth :as auth]
   [finadmin.handlers.dashboard :as dashboard]
   [finadmin.handlers.transactions :as transactions]
   [reitit.ring :as ring]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [ring.middleware.session :refer [wrap-session]]))

(def app
  (-> (ring/ring-handler
       (ring/router
        [;; Login Page
         ["/" {:get auth/login-page}]
         ["/sign-in" {:get auth/sign-in-page :post auth/sign-in-account}]
         ["/sign-up" {:get auth/sign-up-page :post auth/sign-up-account}]

         ;; Dashboard links
         ["/dashboard" {:get dashboard/dashboard-page}]
         ["/overview" {:get dashboard/overview}]
         ["/forms" {:get dashboard/forms}]
         ["/transactions" {:get dashboard/transactions}]
         ["/invoices" {:get dashboard/invoices}]
         ["/expenses" {:get dashboard/expenses}]
         ["/support" {:get dashboard/support}]
         ["/settings" {:get dashboard/settings}]
         ["/logout" {:get dashboard/logout}]

         ;; Interaction
         ["/filter-transactions" {:get transactions/filter-transactions}]
         ["/transaction/:transaction-id/type/:transaction-type" {:get transactions/transaction-details}]
         ["/add-expense" {:post transactions/add-expense}]
         ["/add-invoice" {:post transactions/add-invoice}]
         ["/modify-transaction/:transaction-id/type/:transaction-type" {:get transactions/modify-transaction}]
         ["/delete-transaction/:transaction-id/type/:transaction-type" {:get transactions/delete-transaction}]
         ["/update-password" {:post auth/update-password}]
         ["/delete-account" {:post auth/delete-account}]]
         ))
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      (wrap-session)))
