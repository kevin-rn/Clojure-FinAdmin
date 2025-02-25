(ns finadmin.routes
  (:require
   [finadmin.handlers :as handlers]
   [reitit.ring :as ring]))

(def app
  (ring/ring-handler
   (ring/router
    [["/" {:get handlers/home-page}]
     ["/transactions" {:get handlers/get-transactions
                       :post handlers/add-transaction}]])))
