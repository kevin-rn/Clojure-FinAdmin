(ns finadmin.handlers.auth 
  (:require
   [finadmin.database.accountdb :as accountdb]
   [finadmin.views.authview :as authviews]
   [finadmin.views.dashboardview :as dashviews]))


(defn login-page
  "Handles the rendering of the login page.
     
     Parameters:
     - `request`: A map representing the HTTP request.
     
     Returns:
     - A response map containing the login page if the user is not authenticated.
     - A redirect response to the dashboard if the user is already logged in."
  [request]
  (let [{:keys [email]} (get-in request [:session])]
    (if (nil? email)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (authviews/login)}
      {:status 302
       :headers {"Location" "/dashboard"}})))


(defn sign-in-page
  "Renders the sign-in page.
     
     Returns:
     - A response map containing the sign-in page."
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (authviews/sign-in {}))})


(defn sign-up-page
  "Renders the sign-up page.
     
     Returns:
     - A response map containing the sign-up page."
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str (authviews/sign-up {}))})


(defn sign-in-account
  "Handles the sign-in authentication process.
     
     Parameters:
     - `request`: A map containing user input parameters.
     
     Returns:
     - A response with an error message if the input is invalid or authentication fails.
     - A redirect response to the dashboard upon successful authentication."
  [request]
  (let [{:keys [email password]} (get-in request [:params])]
    (cond
      (or (empty? email) (empty? password))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (authviews/sign-in {:error "All fields are required!"}))}

      (not (accountdb/verify-account? email password))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (authviews/sign-in {:error "Invalid email or password!"}))}

      :else
      {:status 301
       :headers {"HX-Redirect" "/dashboard"}
       :session (assoc (:session request) :email email :password password)})))


(defn sign-up-account
  "Handles user registration.
     
     Parameters:
     - `request`: A map containing user input parameters.
     
     Returns:
     - A response with an error message if input validation fails.
     - A redirect response to the dashboard upon successful registration."
  [request]
  (let [{:keys [email password verify-password]} (get-in request [:params])]
    (cond
      (or (empty? email) (empty? password) (empty? verify-password))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (authviews/sign-up {:error "All fields are required!"}))}

      (not= password verify-password)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (authviews/sign-up {:error "Passwords do not match!"}))}

      (accountdb/email-exists? email)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (authviews/sign-up {:error "Email already exists!"}))}

      :else
      (do
        (accountdb/create-user! email password)
        {:status 301
         :headers {"HX-Redirect" "/dashboard"}
         :session (assoc (:session request) :email email :password password)}))))


(defn update-password
  "Handles password updates for an authenticated user.
     
     Parameters:
     - `request`: A map containing user input parameters.
     
     Returns:
     - A response with an error message if validation fails.
     - A success response with a confirmation modal if the password is successfully updated."
  [request]
  (let [{:keys [current-password new-password verify-password]} (get-in request [:params])
        {:keys [email]} (get-in request [:session])
        current-account (accountdb/get-account-info email current-password)]
    (cond
      (or (empty? current-password) (empty? new-password) (empty? verify-password))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (dashviews/settings-component current-account {:error "All fields are required!"}))}
      
      (not= new-password verify-password)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (str (dashviews/settings-component current-account {:error "Passwords do not match!"}))}
      
      :else
      (do
        (accountdb/update-password-db email new-password)
        (let [new-account (accountdb/get-account-info email new-password)
              modal_message "Your password has been updated succesfully!"]
          {:status 200
           :headers {"Content-Type" "text/html"}
           :body (str (dashviews/settings-component new-account {:modal modal_message}))
           :session (assoc (:session request) :password new-password)})))))


(defn delete-account
  "Handles account deletion.
     
     Parameters:
     - `request`: A map containing the user session data.
     
     Returns:
     - A redirect response to the home page upon successful deletion."
  [request]
  (let [{:keys [email]} (get-in request [:session])]
    (accountdb/delete-account-db email))
  {:status 301
   :headers {"HX-Redirect" "/"}
   :session nil})