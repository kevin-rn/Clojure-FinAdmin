(ns finadmin.db
  (:require
   [buddy.hashers :as hashers]
   [hikari-cp.core :as cp]
   [next.jdbc :as jdbc]))

(def datasource-options {:auto-commit        true
                         :read-only          false
                         :connection-timeout 30000
                         :validation-timeout 5000
                         :idle-timeout       600000
                         :max-lifetime       1800000
                         :minimum-idle       10
                         :maximum-pool-size  10
                         :pool-name          "db-pool"
                         :adapter            "postgresql"
                         :username           "postgres"
                         :password           "postgres"
                         :database-name      "finadmin"
                         :server-name        "localhost"
                         :port-number        5432
                         :register-mbeans    false})

(defonce datasource
  (delay (cp/make-datasource datasource-options)))

(def database-connection {:datasource @datasource})

(defn create-user! 
  "Creates user account in database with secure hashing.
          
      Parameters:
      - email: A string representing the user's email.
      - password: A string representing the user's entered password.
          
      Returns:
      - false if email is nil else executes database query for account creation" 
  [email password]
  (if (nil? email)
    false
    (let [hashed-password (hashers/derive password {:alg :pbkdf2+sha256})]
      (jdbc/execute! database-connection
                     ["INSERT INTO accounts (email, password) VALUES (?, ?)" email hashed-password]))))

  (defn email-exists? 
    "Verifies if the given email match an existing account.
        
           Parameters:
           - email: A string representing the user's email.
        
           Returns:
           - boolean indicating true if email matches that of existing account or else false."
    [email]
    (if (nil? email)
      false
      (let [result (jdbc/execute! database-connection ["SELECT COUNT(1) FROM accounts WHERE email = ?" email])]
        (> (get (first result) :count) 0))))

  (defn verify-account? 
    "Verifies if the given email and password match an existing account.
    
       Parameters:
       - email: A string representing the user's email.
       - password: A string representing the user's entered password.
    
       Returns:
       - boolean indicating true if the password matches the stored hash 
         otherwise false if the email is not found or the password is incorrect."
    [email password]
    (let [query "SELECT password FROM accounts WHERE email = ? LIMIT 1"
          result (jdbc/execute! database-connection [query email])]
      (if (seq result)
        (let [stored-hash (get-in (first result) [:accounts/password])]
          (hashers/verify password stored-hash {:alg :pbkdf2+sha256}))
        false)))
