(ns finadmin.views.authview 
    (:require
     [hiccup.page :refer [include-css include-js]]
     [hiccup2.core :as h]))

(defn sign-in
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
  [{:keys [error]}]
  (h/html [:div {:id "sign-up" :class "w-full"}
           [:h1 {:class "text-center sign-title"} "Sign Up"]
           [:form {:class "flex flex-col items-center"}
            (when error
              [:div.error-message
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
          [:link {:rel "icon" :href "/logo/favicon.ico" :type "image/x-icon"}]]
         [:body {:class "bg-[url(../img/login-background.png)] login-page"}
          [:div {:class "flex h-screen"}
           [:div {:class "w-2/3 flex items-center justify-center"}
            [:div {:class "border-solid w-2/3 welcome"}
             [:h1 "Start your Financial Administration here!"]
             [:h3 "Built with Clojure"]]]
           [:div {:id "auth-container" :class "w-1/3 flex items-center justify-center sign-div"}
            (sign-in {})]]]])))