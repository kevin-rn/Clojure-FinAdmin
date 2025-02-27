(ns finadmin.routes
  (:require
   [finadmin.handlers :as handlers]
   [reitit.ring :as ring]
   [ring.middleware.defaults :refer [wrap-defaults]]))

(def app
  (-> (ring/ring-handler
       (ring/router
        [["/" {:get handlers/login-page}]
         ["/sign-in" {:get handlers/sign-in-page}]
         ["/sign-up" {:get handlers/sign-up-page}]
         ["/dashboard" {:get handlers/dashboard-page}]]))
       (wrap-defaults {:static {:resources "public"
                                :files     "resources/public"}})))
