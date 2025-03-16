(ns finadmin.views.authview 
    (:require
     [hiccup.page :refer [include-css include-js]]
     [hiccup2.core :as h]))


(defn sign-in
  "Renders the sign-in page where users can log in with their email and password. 
     If there is an error (e.g., invalid credentials), it displays the error message.
       
       Parameters:
       - `{:keys [error]}`: A map containing an optional error message to display if login fails.
       
       Returns:
       - A string containing the HTML for the sign-in form, including optional error messages."
  [{:keys [error]}]
  (h/html [:div {:id "sign-in" :class "w-full"}
           [:h1 {:class "text-center sign-title"} "Sign In"]
           [:form {:class "flex flex-col items-center"}
           (when error
             [:div {:class "error-message flex items-center"}
              [:img {:src "/icons/error.svg" :alt "Error message"}]
              [:p error]])
            [:div.input-group
             [:span.email-icon]
             [:input {:type "email" :name "email" :required true}]
             [:label {:for "email" :class "labeltext"} "Email"]]
            [:div.input-group
             [:span.password-icon]
             [:input {:type "password" :class "password" :name "password" :required true}]
             [:label {:for "password" :class "labeltext"} "Password"]
             [:input {:type "checkbox" :id "visibility" :class "visibility toggle-password" :onclick "togglePassword(this)"}]
             [:label {:for "visibility" :class "visibility-icon hidden"}]]

            [:button {:type "submit" :hx-post "/sign-in" :hx-target "#sign-in"} "Sign In"]

            [:div {:class "text-center switch-sign"}
             [:span "Don't have an account? "]
             [:a {:href "#" :class "sign-text" :hx-get "/sign-up" :hx-target "#auth-container"} "Sign Up"]]]]))


(defn sign-up
  "Renders the sign-up page where new users can create an account by entering their email and password.
     It also includes a field to verify the password and displays error messages if necessary.
       
       Parameters:
       - `{:keys [error]}`: A map containing an optional error message to display if the registration fails.
       
       Returns:
       - A string containing the HTML for the sign-up form, including optional error messages."
  [{:keys [error]}]
  (h/html [:div {:id "sign-up" :class "w-full"}
           [:h1 {:class "text-center sign-title"} "Sign Up"]
           [:form {:class "flex flex-col items-center"}
            (when error
              [:div {:class "error-message flex items-center"}
               [:img {:src "/icons/error.svg" :alt "Error message"}]
               [:p error]])
            [:div.input-group
             [:span.email-icon]
             [:input {:type "email" :name "email" :required true}]
             [:label {:for "email" :class "labeltext"} "Email"]]
            [:div.input-group
             [:span.password-icon]
             [:input {:type "password" :class "password" :name "password" :required true}]
             [:label {:for "password" :class "labeltext"} "Password"]
             [:input {:type "checkbox" :id "new-visibility" :class "visibility toggle-password" :onclick "togglePassword(this)"}]
             [:label {:for "new-visibility" :class "visibility-icon hidden"}]]
            [:div.input-group
             [:span.password-icon]
             [:input {:type "password" :class "password" :name "verify-password" :required true}]
             [:label {:for "verify-password" :class "labeltext"} "Repeat Password"]
             [:input {:type "checkbox" :id "repeat-visibility" :class "visibility toggle-password" :onclick "togglePassword(this)"}]
             [:label {:for "repeat-visibility" :class "visibility-icon hidden"}]]

            [:button {:type "submit" :hx-post "/sign-up" :hx-target "#sign-up" :hx-swap "OuterHTML"} "Sign Up"]

            [:div {:class "text-center switch-sign"}
             [:span "Already have an account? "]
             [:a {:href "#" :class "sign-text" :hx-get "/sign-in" :hx-target "#auth-container"} "Sign In"]]]]))


(defn login
   "Renders the login page, which includes the sign-in form, and some welcome text for users who are not yet authenticated.
     It includes links for users to either sign in or sign up for an account.
       
       Returns:
       - A string containing the full HTML structure of the login page, including external resources like CSS, JS, and fonts."
  []
  (str "<!DOCTYPE html>"
       (h/html
        [:html
         [:head
          [:title "Clojure FinAdmin"]
          (include-css "/css/output.css")
          (include-css "/css/login.css")
          (include-js "https://unpkg.com/htmx.org@2.0.4")
          (include-js "https://code.jquery.com/jquery-3.6.0.min.js")
          [:script {:src "/js/app.js" :defer true}]
          [:link {:href "https://fonts.googleapis.com/css?family=Montserrat:400,900" :rel "stylesheet"}]
          [:link {:rel "shortcut icon" :href "/favicon.ico" :type "image/x-icon"}]]
         [:body {:class "bg-[url(../img/login-background.png)] login-page"}
          [:div {:class "flex h-screen"}
           [:div {:class "w-2/3 flex items-center justify-center"}
            [:div {:class "border-solid w-2/3 welcome"}
             [:h1 "Start your Financial Administration here!"]
             [:h3 "Built with Clojure"]]]
           [:div {:id "auth-container" :class "w-1/3 flex items-center justify-center sign-div"}
            (sign-in {})]]]])))