(ns finadmin.routes
  (:require
   [finadmin.handlers :as handlers]
   [reitit.ring :as ring]
   [ring.middleware.defaults :refer [wrap-defaults]]))

(def app
  (-> (ring/ring-handler
       (ring/router
        [["/" {:get handlers/home-page}]
         ["/transactions" {:get handlers/get-transactions
                           :post handlers/add-transaction}]]))
      (wrap-defaults {:static {:resources "public"
                               :files     "resources/public"}})))
