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
          [:link {:rel "icon" :href "/logo/favicon.ico" :type "image/x-icon"}]]
         [:body {:class "bg-[url(../img/login-background.png)] login-page"}
          [:div {:class "flex h-screen"}
           [:div {:class "w-2/3 flex items-center justify-center"}
            [:div {:class "border-solid w-2/3 welcome"}
             [:h1 "Start your Financial Administration here!"]
             [:h3 "Built with Clojure"]]]
           [:div {:id "auth-container" :class "w-1/3 flex items-center justify-center sign-div"}
            (sign-in {})]]]])))

(defn overview-component
  []
  (h/html
   [:p "overview"]
   ))

(defn forms-component
  []
  (h/html
   [:div
    [:h2 "Upload Forms or Documents"]
    [:button "Upload"]]
    
   [:div
    [:h2 "Documents:"]
    [:ul
     [:li "Some documents"]]

    ]))

(defn transactions-component
  []
  (let [transactions (repeat 50 {:date "2024-03-09" :amount "$150.00" :currency "USD" :type "Invoice" :description "Consulting service" :status "Paid"})]
    (h/html
     [:div {:id "transactions-container" :class "h-[80vh] flex flex-col"}
      [:h2 "Transaction History"]
      
      [:div
       [:label {:for "transaction-type"} "Transaction Type"]
       [:select {:name "transaction-type" :hx-get "/filter-transactions" :hx-target "#transaction-list" :hx-trigger "change"}
        [:option {:value "all"} "All"]
        [:option {:value "invoice"} "Invoices"]
        [:option {:value "expense"} "Expenses"]]]

      [:div {:class "flex-1 overflow-auto mt-8"}
       [:table {:class "w-full border-collapse"}

        [:thead {:class "border-b-2 sticky top-0 z-10"}
         [:tr
          [:th {:class "p-3 text-sm font-semibold tracking-wide text-left"} "Date"]
          [:th {:class "p-3 text-sm font-semibold tracking-wide text-left"} "Amount"]
          [:th {:class "p-3 text-sm font-semibold tracking-wide text-left"} "Currency"]
          [:th {:class "p-3 text-sm font-semibold tracking-wide text-left"} "Type"]
          [:th {:class "p-3 text-sm font-semibold tracking-wide text-left"} "Description"]
          [:th {:class "p-3 text-sm font-semibold tracking-wide text-left"} "Status"]]]

        [:tbody
         (for [{:keys [date amount currency type description status]} transactions]
           [:tr {:class "border-b"}
            [:td {:class "p-3"} date]
            [:td {:class "p-3"} amount]
            [:td {:class "p-3"} currency]
            [:td {:class "p-3"} type]
            [:td {:class "p-3"} description]
            [:td {:class "p-3"} status]])]]]])))





(defn invoices-component
  []
  (h/html
   [:div {:id "invoice-form"}
    [:h2 "Add Invoice"]
    [:form {:class "flex flex-col items-center space-y-6"}
     [:div {:class "input-group"}
      [:label "Due Date"]
      [:input {:type "date" :name "due-date" :required true}]]
     [:div {:class "input-group"}
      [:label "Amount"]
      [:input {:type "number" :name "amount" :step "0.01" :min "0" :required true}]]
     [:div {:class "input-group"}
      [:label "Currency"]
      [:input {:type "text" :name "currency" :required true}]]
     [:div {:class "input-group"}
      [:label "Status"]
      [:select {:name "status"}
       [:option {:value "unpaid"} "Unpaid"]
       [:option {:value "paid"} "Paid"]]]
     [:button {:type "submit" :hx-post "/add-invoice" :hx-target "#dashboard-content" :hx-swap "innerHTML"} "Submit"]]]

    [:div
     [:h2 "Invoice List"]
     [:ul
      [:li "Sample Invoice"]]]))

(defn expenses-component
  []
  (h/html
   [:div {:class "expenses-container"}
    [:h2 "Add Expense"]
    [:form {:class "flex flex-col items-center" :hx-post "/add-expense" :hx-target "#dashboard-content" :hx-swap "outerHTML"}
     [:div {:class "input-group"}
      [:label {:for "expense-category"} "Category"]
      [:select {:name "expense-category" :required true}
       [:option {:value "office-supplies"} "Office Supplies"]
       [:option {:value "travel"} "Travel"]
       [:option {:value "utilities"} "Utilities"]
       [:option {:value "other"} "Other"]]]
     [:div {:class "input-group"}
      [:label {:for "amount"} "Amount"]
      [:input {:type "number" :name "amount" :step "0.01" :required true}]]
     [:div {:class "input-group"}
      [:label {:for "currency"} "Currency"]
      [:select {:name "currency"}
       [:option {:value "USD"} "USD"]
       [:option {:value "EUR"} "EUR"]
       [:option {:value "GBP"} "GBP"]]]
     [:div {:class "input-group"}
      [:label {:for "description"} "Description"]
      [:textarea {:name "description" :rows "3"}]]
     [:div {:class "input-group"}
      [:label {:for "status"} "Status"]
      [:select {:name "status"}
       [:option {:value "pending"} "Pending"]
       [:option {:value "approved"} "Approved"]]]
     [:button {:type "submit"} "Submit Expense"]]]

   [:div {:class "expenses-list"}
    [:h2 "Expense List"]
    [:ul {:id "expense-list"}
     [:li "Example Expense 1"]
     [:li "Example Expense 2"]]]))


(defn settings-component
  [email]
  (h/html
   [:div 
    [:h2 "Profile Settings"]
    [:ul
     [:li 
      [:p (str "Email: " email)]
      [:span ""]]
     [:li 
      [:p "Password"]]]]
   
   [:div 
    [:h2 "Update Password"]
    [:form
     [:input]
     [:input]]]
   
   [:button "Delete Account"]
   )
)

(defn support-component
  []
  (h/html
   [:div
    [:h2 "Frequently Asked Questions"]
    [:ul
     [:li [:strong "what features does this app have?"] [:p "In this amazing webapp you can add, edit and delete your financial transactions."]]
     [:li [:strong "What type of financial transactions are supported?"] [:p "Only invoices and expenses are supported."]]
     [:li [:strong "How do I reset my password?"] [:p "Go to the settings tab, and follow the steps on the bottom to update your password."]]
     [:li [:strong "Will this webapp have more features?"] [:p "No."]]]]

   [:div
    [:h2 "Feedback"]
    [:p "We value your feedback. Please let us know how we can improve."]
    [:textarea {:placeholder "Your feedback..." :rows "4" :cols "50"}]
    [:button "Submit Feedback"]])
   )

(defn dashboard
  [email]
  (str "<!DOCTYPE html>"
       (h/html
        [:html
         [:head
          [:title "Clojure FinAdmin"]
          (include-css "/css/output.css")
          (include-css "/css/dashboard.css")
          (include-js "https://unpkg.com/htmx.org@2.0.4")
          (include-js "https://code.jquery.com/jquery-3.6.0.min.js")
          [:script {:src "/js/app.js" :defer true}]
          [:link {:href "https://fonts.googleapis.com/css?family=Montserrat:400,900" :rel "stylesheet"}]
          [:link {:rel "icon" :href "/logo/favicon.ico" :type "image/x-icon"}]]
         [:body
          [:header
           [:img {:src "/logo/logo.png" :alt "Logo" :class "logo"}]
           [:p "Financial Administration"]
           [:p email]]

          [:div {:class "flex"}
           [:nav {:id "sidebar" :class "flex"}
            [:ul
             [:li
              [:button {:id "toggle-btn" :onclick "toggleSidebar()"}
               [:img {:src "/icons/toggle.svg" :alt "Toggle button"}]]]
             [:li {:class "selected"} [:a {:href "" :hx-get "/overview" :hx-target "#dashboard-content"}
                                     [:img {:src "/icons/dashboard.svg" :alt "Dashboard Icon"}]
                                     [:span "Overview"]]]
             [:li [:a {:href "" :hx-get "/forms" :hx-target "#dashboard-content"}
                   [:img {:src "/icons/forms.svg" :alt "Forms Icon"}]
                   [:span "Forms"]]]
             [:li
              [:button {:class "dropdown-btn" :onclick "toggleSubMenu(this)"}
               [:img {:src "/icons/bookkeeping.svg" :alt "Bookkeeping"}]
               [:span "Bookkeeping"]
               [:img {:src "/icons/dropdown.svg" :alt "Dropdown Menu"}]]
              [:ul {:class "sub-menu"}
               [:div
                [:li [:a {:href "" :hx-get "/transactions" :hx-target "#dashboard-content"}
                      [:img {:src "/icons/balance.svg" :alt "Transactions Icon"}]
                      [:span "Transaction History"]]]
                [:li [:a {:href "" :hx-get "/expenses" :hx-target "#dashboard-content"}
                      [:img {:src "/icons/expenses.svg" :alt "Expenses Icon"}]
                      [:span "Expenses"]]]
                [:li [:a {:href "" :hx-get "/invoices" :hx-target "#dashboard-content"}
                      [:img {:src "/icons/invoices.svg" :alt "Invoices Icon"}]
                      [:span "Invoices"]]]]]]
             [:li [:a {:href "" :hx-get "/settings" :hx-target "#dashboard-content"}
                   [:img {:src "/icons/settings.svg" :alt "Settings Icon"}]
                   [:span "Settings"]]]
             [:li [:a {:href "" :hx-get "/support" :hx-target "#dashboard-content"}
                   [:img {:src "/icons/support.svg" :alt "Support Icon"}]
                   [:span "Support"]]]
             [:li [:a {:href "/" :hx-get "/logout" :hx-target "this"}
                   [:img {:src "/icons/logout.svg" :alt "Log out Icon"}]
                   [:span "Log out"]]]]]

           [:main {:class "flex items-center justify-center"}
            [:div {:id "dashboard-content" :class "min-w-3/4"}
             (overview-component)]]]]])))
