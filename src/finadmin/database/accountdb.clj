(ns finadmin.database.accountdb
  (:require
   [buddy.hashers :as hashers]
   [finadmin.database.datasource :refer [database-connection]]
   [next.jdbc :as jdbc]))


(defn create-user!
  "Creates a user account in the database with a secure hashed password.

   Parameters:
   - email: A string representing the user's email address.
   - password: A string representing the user's chosen password.
   
   Returns:
   - false if the email is nil; otherwise, returns nil after executing the database query to create the account."
  [email password]
  (if (nil? email)
    false
    (let [hashed-password (hashers/derive password {:alg :pbkdf2+sha256})]
      (jdbc/execute-one! database-connection
                         ["INSERT INTO accounts (email, password) VALUES (?, ?)" email hashed-password]))))


(defn email-exists?
  "Checks if the provided email exists in the database.

   Parameters:
   - email: A string representing the user's email address.
   
   Returns:
   - boolean indicating true if the email matches an existing account, false otherwise."
  [email]
  (if (nil? email)
    false
    (let [result (jdbc/execute! database-connection ["SELECT COUNT(1) FROM accounts WHERE email = ?" email])]
      (> (get (first result) :count) 0))))


(defn verify-account?
  "Verifies if the given email and password match an existing account.

   Parameters:
   - email: A string representing the user's email address.
   - password: A string representing the user's entered password.
   
   Returns:
   - true if the email exists and the provided password matches the stored hashed password; 
   - false if the email doesn't exist or the password is incorrect."
  [email password]
  (let [query "SELECT password FROM accounts WHERE email = ?"
        result (jdbc/execute-one! database-connection [query email])]
    (when-let [stored-hash (:accounts/password result)]
      (:valid (hashers/verify password stored-hash {:alg :pbkdf2+sha256})))))


(defn get-account-info
  "Retrieves all information for given email and password match an existing account.

   Parameters:
   - email: A string representing the user's email address.
   - password: A string representing the user's entered password.
   
   Returns:
   - Account information (email, password, date created)
   - false if the email doesn't exist or the password is incorrect."
  [email password]
  (let [query "SELECT * FROM accounts WHERE email = ?"
        result (jdbc/execute-one! database-connection [query email])]
    (if (and result (hashers/verify password (:accounts/password result) {:alg :pbkdf2+sha256}))
      (assoc result :accounts/password password)
      false)))


(defn update-password-db
  "Updates the password for a given user account.
    
    Parameters:
    - `email`: A string representing the user's email address.
    - `new-password`: A string representing the new password to be set.
    
    The function hashes the new password using PBKDF2+SHA256 before updating the database.
    
    Returns the result of the update operation."
  [email new-password]
  (let [query "UPDATE accounts SET password = ? WHERE email = ?"
        hashed-new-password (hashers/derive new-password {:alg :pbkdf2+sha256})]
    (jdbc/execute-one! database-connection [query hashed-new-password email])))


(defn delete-account-db
  "Deletes a user account and all associated transactions (on cascade also invoices and expenses).
   
   Parameters:
   - email: A string representing the email of the account to delete."
  [email]
  (jdbc/with-transaction [dc database-connection]
    (jdbc/execute-one! dc ["DELETE FROM accounts WHERE email = ?", email])
    (jdbc/execute-one! dc ["DELETE FROM transactions WHERE account_email = ?", email])))