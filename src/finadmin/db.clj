(ns finadmin.db
  (:require
   [buddy.hashers :as hashers]
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

(defn email-exists? [email]
  (let [result (jdbc/execute! (get-conn) ["SELECT COUNT(1) FROM accounts WHERE email = ?" email])]
    (> (get (first result) :count) 0)))

(defn create-user! [email password]
  (let [hashed-password (hashers/derive password)]
    (jdbc/execute! (get-conn)
                   ["INSERT INTO accounts (email, password) VALUES (?, ?)" email hashed-password])))
