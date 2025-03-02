(ns finadmin.core
  (:require
   [clojure.tools.logging :as log]
   [finadmin.routes :refer [app]]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.reload :refer [wrap-reload]]))

(defn -main []
  (log/info "Starting server on port 3000...")
  (run-jetty (wrap-reload app) {:port 3000, :join? false})
  (log/info "Server started on http://localhost:3000/"))