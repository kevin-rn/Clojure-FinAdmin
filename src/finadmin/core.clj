(ns finadmin.core
  (:require
   [finadmin.routes :refer [app]]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.reload :refer [wrap-reload]]))

(defn -main []
  (run-jetty (wrap-reload app) {:port 3000, :join? false}))
  (println "Server started on port 3000: http://localhost:3000/")