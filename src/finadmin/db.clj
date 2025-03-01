(ns finadmin.db
  (:require
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as connection]) 
  (:import
   [com.zaxxer.hikari HikariDataSource]))

(def db-spec
  {:dbtype   "postgresql"
   :host     "localhost"
   :port     5432
   :dbname   "finadmin"
   :user     "admin"
   :password "pass123"})

(defonce datasource
  (connection/->pool HikariDataSource db-spec))

(defn get-conn []
  {:datasource datasource})


(defn get-users []
  (jdbc/execute! (get-conn) ["SELECT * FROM accounts"]))
