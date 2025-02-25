(ns finadmin.core
  (:require
   [finadmin.routes :refer [app]]
   [ring.adapter.jetty :refer [run-jetty]]))

(defn -main []
  (run-jetty app {:port 3000, :join? false}))