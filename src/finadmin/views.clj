(ns finadmin.views
  (:require
   [hiccup.page :refer [include-css include-js]]
   [hiccup2.core :as h]
   [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn sign-in 
  [{:keys [error]}]
  (h/html [:div {:id "sign-in" :class "w-full"}
           [:h1 {:class "text-center sign-title"} "Sign In"] 
           [:form {:class "flex flex-col items-center"}
            (when error
              [:div {:class "error-message flex items-center"}
               [:img {:src "/icons/error.svg" :alt "Error message"}]
               [:p error]])
            (h/raw (anti-forgery-field))
            [:div {:class "input-group"}
             [:span {:class "email-icon"}]
             [:input {:type "email" :name "email" :required true}]
             [:label {:for ""} "Email"]]
            [:div {:class "input-group"}
             [:span {:class "password-icon"}]
             [:input {:type "password" :name "password" :required true}]
             [:label {:for ""} "Password"]]
            [:button {:type "submit" :hx-post "/sign-in" :hx-target "#sign-in" :hx-swap "outerHTML"} "Sign In"]

            [:div {:class "text-center switch-sign"}
             [:span "Don't have an account? "]
             [:a {:href "#" :class "sign-text" :hx-get "/sign-up" :hx-target "#auth-container"} "Sign Up"]]]]))

(defn sign-up 
  [{:keys [error]}]
  (h/html [:div {:id "sign-up" :class "w-full"}
           [:h1 {:class "text-center sign-title"} "Sign Up"] 
           [:form {:class "flex flex-col items-center"}
            (when error
              [:div {:class "error-message"}
               [:img {:src "/icons/error.svg" :alt "Error message"}]
               [:p error]])
            (h/raw (anti-forgery-field))
            [:div {:class "input-group"}
             [:span {:class "email-icon"}]
             [:input {:type "email" :name "email" :required true}]
             [:label "Email"]]
            [:div {:class "input-group"}
             [:span {:class "password-icon"}]
             [:input {:type "password" :name "password" :required true}]
             [:label "Password"]]
            [:div {:class "input-group"}
             [:span {:class "password-icon"}]
             [:input {:type "password" :name "repeat-password" :required true}]
             [:label "Repeat Password"]]
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
          [:link {:href "https://fonts.googleapis.com/css?family=Montserrat:400,900" :rel "stylesheet"}]
          [:link {:rel "icon" :href "/favicon.ico" :type "image/x-icon"}]]
         [:body {:class "bg-[url(../img/login-background.png)] login-page"}
          [:div {:class "flex h-screen"}
           [:div {:class "w-2/3 flex items-center justify-center"}
            [:div {:class "border-solid w-2/3 welcome"}
             [:h1 "Start your Financial Administration here!"]
             [:h3 "Built with Clojure"]]]
           [:div {:id "auth-container" :class "w-1/3 flex items-center justify-center sign-div"}
            (sign-in {})]]]])))

(defn dashboard 
  [email]
  (let [email (if (nil? email) "Guest" email)]
    (str "<!DOCTYPE html>"
         (h/html
          [:html
           [:head
            [:title "Clojure FinAdmin"]
            (include-css "/css/output.css")
            (include-css "/css/dashboard.css")
            (include-js "https://unpkg.com/htmx.org@2.0.4")
            [:script {:src "/js/app.js" :defer true}]
            [:link {:href "https://fonts.googleapis.com/css?family=Montserrat:400,900" :rel "stylesheet"}]
            [:link {:rel "icon" :href "/logo/favicon.ico" :type "image/x-icon"}]]
           [:body
            [:header
             [:img {:src "/logo/logo.png" :alt "Logo" :class "logo"}]
             [:p "Financial Administration"]
             [:p email]]

            [:nav {:id "sidebar" :class "flex"}
             [:ul
              [:li
               [:button {:id "toggle-btn" :onclick "toggleSidebar()"}
                [:img {:src "/icons/toggle.svg" :alt "Toggle button"}]]]
              [:li {:class "active"} [:a
                                      [:img {:src "/icons/dashboard.svg" :alt "Dashboard Icon"}]
                                      [:span "Dashboard"]]]
              [:li [:a
                    [:img {:src "/icons/forms.svg" :alt "Forms Icon"}]
                    [:span "Forms"]]]
              [:li
               [:button {:class "dropdown-btn" :onclick "toggleSubMenu(this)"}
                [:img {:src "/icons/bookkeeping.svg" :alt "Bookkeeping"}]
                [:span "Bookkeeping"]
                [:img {:src "/icons/dropdown.svg" :alt "Dropdown Menu"}]]
               [:ul {:class "sub-menu"}
                [:div
                 [:li [:a
                       [:img {:src "/icons/balance.svg" :alt "Account Balance Icon"}]
                       [:span "Account Balance"]]]
                 [:li [:a
                       [:img {:src "/icons/expenses.svg" :alt "Expenses Icon"}]
                       [:span "Expenses"]]]
                 [:li [:a
                       [:img {:src "/icons/invoices.svg" :alt "Invoices Icon"}]
                       [:span "Invoices"]]]]]]
              [:li [:a
                    [:img {:src "/icons/settings.svg" :alt "Settings Icon"}]
                    [:span "Settings"]]]
              [:li [:a
                    [:img {:src "/icons/support.svg" :alt "Support Icon"}]
                    [:span "Support"]]]
              [:li [:a {:href "/" :hx-get "/logout" :hx-target "this"}
                    [:img {:src "/icons/logout.svg" :alt "Log out Icon"}]
                    [:span "Log out"]]]]]

            [:main {:class "flex items-center justify-center"}]]]))))
