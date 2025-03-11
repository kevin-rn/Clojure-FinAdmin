(ns finadmin.views
  (:require
   [hiccup.page :refer [include-css include-js]]
   [hiccup2.core :as h]
   [ring.util.anti-forgery :refer [anti-forgery-field]])
  (:import
   [java.sql Timestamp]
   [java.time.format DateTimeFormatter]
   [java.util Locale]))


(defn parse-and-format-date [datetime]
  (if (instance? Timestamp datetime)
    (let [output-formatter (.withLocale (DateTimeFormatter/ofPattern "d MMMM yyyy - HH:mm:ss") Locale/ENGLISH)
          local-datetime (.toLocalDateTime datetime)]
      (.format local-datetime output-formatter))
    (throw (IllegalArgumentException. "Expected a java.sql.Timestamp object"))))

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
            [:div.input-group
             [:span.email-icon]
             [:input {:type "email" :name "email" :required true}]
             [:label {:for ""} "Email"]]
            [:div.input-group
             [:span.password-icon]
             [:input {:type "password" :name "password" :required true}]
             [:label {:for ""} "Password"]]
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
            (h/raw (anti-forgery-field))
            [:div.input-group
             [:span.email-icon]
             [:input {:type "email" :name "email" :required true}]
             [:label "Email"]]
            [:div.input-group
             [:span.password-icon]
             [:input {:type "password" :name "password" :required true}]
             [:label "Password"]]
            [:div.input-group
             [:span.password-icon]
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
   [:div
    [:p "overview"]
    ]
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
     [:li "Some documents"]]]))



(defn transactions-list
  [transactions]
  (h/html
   [:table {:class "w-full border-collapse"}

    [:thead {:class "border-b-2 sticky top-0 z-10"}
     [:tr
      [:th {:class "p-3 text-sm font-semibold tracking-wide text-left"} "Date"]
      [:th {:class "p-3 text-sm font-semibold tracking-wide text-left"} "Amount"]
      [:th {:class "p-3 text-sm font-semibold tracking-wide text-left"} "Currency"]
      [:th {:class "p-3 text-sm font-semibold tracking-wide text-left"} "Type"]
      [:th {:class "p-3 text-sm font-semibold tracking-wide text-left"} "Description"]
      [:th {:class "p-3 text-sm font-semibold tracking-wide text-left"} "Payment method"]]]

    (if (empty? transactions)
      [:tbody
       [:tr {:class "text-center"}
        [:td {:colspan "6" :class "p-3"} [:i {:class "select-none"} "No Transactions stored"]]]]
      [:tbody
       (for [{:transactions/keys [transaction_date amount currency transaction_type description payment_method]} transactions]
         [:tr {:class "border-b"}
          [:td {:class "p-3"} (parse-and-format-date transaction_date)]
          [:td {:class "p-3"} amount]
          [:td {:class "p-3"} currency]
          [:td {:class "p-3"} transaction_type]
          [:td {:class "p-3"} description]
          [:td {:class "p-3"} payment_method]])])]))

(defn transactions-component
  [transactions]
  (h/html
   [:div {:id "transactions-container" :class "h-[80vh] flex flex-col"}
    [:h2 "Transaction History"]

    [:div
     [:label {:for "transaction-type"} "Transaction Type"]
     [:select {:name "transaction-type" :hx-get "/filter-transactions" :hx-target "#transaction-list" :hx-trigger "change"}
      [:option {:value "all"} "All"]
      [:option {:value "invoice"} "Invoices"]
      [:option {:value "expense"} "Expenses"]]]

    [:div {:class "flex-1 overflow-auto mt-8" :id "transaction-list"}
     (transactions-list transactions)]]))

(defn expenses-component
  []
  (h/html
   [:div {:id "expense-form"}
    [:h2 "Add Expense"]
    [:form {:class "grid grid-cols-3 gap-4"
            :hx-post "/add-expense"}

     ;; Expense Details
     [:fieldset {:class "col-span-3 border p-4 rounded"}
      [:legend "Expense Details"]
      [:div {:class "grid grid-cols-3 gap-4"}
       [:div {:class "input-group"}
        [:label "Transaction Date:"]
        [:input {:type "date" :name "transaction_date" :required true}]]
       [:div {:class "input-group"}
        [:label "Amount:"]
        [:input {:type "number" :name "amount" :step "0.01" :min "0" :required true}]]
       [:div {:class "input-group"}
        [:label "Currency:"]
        [:input {:type "text" :name "currency" :required true :value "EUR" :maxlength "3" :autocapitalize "characters"}]]
       [:div {:class "input-group col-span-3"}
        [:label "Description:"]
        [:textarea {:name "description" :class "w-full h-48 resize-none"}]]]]

     ;; Payment Information
     [:fieldset {:class "col-span-3 border p-4 rounded"}
      [:legend "Payment Information"]
      [:div {:class "grid grid-cols-2 gap-4"}
       [:div {:class "input-group"}
        [:label "Expense Type:"]
        [:input {:type "text" :name "expense_type"}]]
       [:div {:class "input-group"}
        [:label "Payment Method:"]
        [:select {:name "payment_method"}
         [:option {:value "credit_card"} "Credit Card"]
         [:option {:value "cash"} "Cash"]
         [:option {:value "bank_transfer"} "Bank Transfer"]
         [:option {:value "cheque"} "Cheque"]
         [:option {:value "installment_payment"} "Installment Payment"]]]]]

     ;; Expense Classification
     [:fieldset {:class "col-span-3 border p-4 rounded"}
      [:legend "Expense Classification"]
      [:div {:class "grid grid-cols-2 gap-4"}
       [:div {:class "input-group"}
        [:label "Reimbursement Status:"]
        [:select {:name "reimbursement_status"}
         [:option {:value "pending"} "Pending"]
         [:option {:value "approved"} "Approved"]
         [:option {:value "reimbursed"} "Reimbursed"]]]
       [:div {:class "input-group col-span-2"}
        [:label "Business Purpose:"]
        [:input {:type "text" :name "business_purpose"}]]
       [:div {:class "input-group"}
        [:label "Approval Status:"]
        [:select {:name "approval_status"}
         [:option {:value "pending"} "Pending"]
         [:option {:value "approved"} "Approved"]
         [:option {:value "rejected"} "Rejected"]]]
       [:div {:class "input-group"}
        [:label "Expense Date:"]
        [:input {:type "date" :name "expense_date" :required true}]]]]

     [:button {:type "submit"} "Submit"]]]))

(defn invoices-component
  []
  (h/html
   [:div {:id "invoice-form"}
    [:h2 "Add Invoice"]
    [:form {:class "grid grid-cols-3 gap-4"
            :hx-post "/add-invoice"
            :hx-target "#invoice-list"}

     ;; Invoice Details
     [:fieldset {:class "col-span-3 border p-4 rounded"}
      [:legend "Invoice Details"]
      [:div {:class "grid grid-cols-3 gap-4"}
       [:div {:class "input-group"}
        [:label "Invoice Number:"]
        [:input {:type "text" :name "invoice_number"}]]
       [:div {:class "input-group"}
        [:label "Amount:"]
        [:input {:type "number" :name "amount" :step "0.01" :min "0" :required true}]]
       [:div {:class "input-group"}
        [:label "Currency:"]
        [:input {:type "text" :name "currency" :required true :value "EUR" :maxlength "3" :autocapitalize "characters"}]]
       [:div {:class "input-group col-span-3"}
        [:label "Description:"]
        [:textarea {:name "description" :class "w-full h-48 resize-none"}]]]]

     ;; Vendor Information
     [:fieldset {:class "col-span-3 border p-4 rounded"}
      [:legend "Vendor Information:"]
      [:div {:class "grid grid-cols-3 gap-4"}
       [:div {:class "input-group"}
        [:label "Vendor Name:"]
        [:input {:type "text" :name "vendor_name"}]]
       [:div {:class "input-group"}
        [:label "PO Number:"]
        [:input {:type "text" :name "po_number"}]]
       [:div {:class "input-group"}
        [:label "VAT code:"]
        [:input {:type "text" :name "vat_code"}]]]]

     ;; Payment Information
     [:fieldset {:class "col-span-3 border p-4 rounded"}
      [:legend "Payment Information"]
      [:div {:class "grid grid-cols-2 gap-4"}
       [:div {:class "input-group"}
        [:label {:class "col-span-2"} "Payment Method:"]
        [:input {:type "text" :class "col-span-2" :name "payment_method"}]]
       [:div {:class "input-group"}
        [:label "Payment Terms:"]
        [:input {:type "text" :name "payment_terms"}]]
       [:div {:class "input-group"}
        [:label "Due Date:"]
        [:input {:type "date" :name "due-date" :required true}]]
       [:div {:class "input-group"}
        [:label "Payment Status:"]
        [:select {:name "payment_status"}
         [:option {:value "pending"} "Pending"]
         [:option {:value "unpaid"} "Unpaid"]
         [:option {:value "paid"} "Paid"]]]]]

     [:button {:type "submit"} "Submit"]]]))


(defn settings-component
  [account {:keys [error]}]
  (h/html
   [:div#settings-form
    [:h2 "Profile"]
    [:div {:class "grid grid-cols-6 grid-rows-1 gap-4"}
     [:div {:class "col-span-1"}
      [:p "Email:"]
      [:p "Password:"]
      [:p "Created at:"]]

      [:div {:class "col-span-3"}
       [:p [:i (:accounts/email account)]] 
       [:p [:i (:accounts/password account)]] 
       [:p [:i (parse-and-format-date (:accounts/created_at account))]]]]]

   [:div
    [:h2 "Update Password"]
    (when error
      [:div.error-message
       [:img {:src "/icons/error.svg" :alt "Error message"}]
       [:p error]])

    [:form
     [:div {:class "input-group grid grid-cols-6 grid-rows-1 gap-4"}
      [:label {:for "current-password"} "Current Password"]
      [:input {:class "password" :type "password" :name "current-password" :value ""}]
      [:div {:class "checkbox"}
       [:input {:type "checkbox" :class "toggle-password" :onclick "togglePassword(this)"}]
       [:i "Show Password"]]]

     [:div {:class "input-group grid grid-cols-6 grid-rows-1 gap-4"}
      [:label {:for "new-password"} "New Password"]
      [:input {:class "password" :type "password" :name "new-password" :value ""}]
      [:div {:class "checkbox"}
       [:input {:type "checkbox" :class "toggle-password" :onclick "togglePassword(this)"}]
       [:i "Show Password"]]]

     [:div {:class "input-group grid grid-cols-6 grid-rows-1 gap-4"}
      [:label {:for "verify-password"} "Verify New Password"]
      [:input {:class "password" :type "password" :name "verify-password" :value ""}]
      [:div {:class "checkbox"}
       [:input {:type "checkbox" :class "toggle-password" :onclick "togglePassword(this)"}]
       [:i "Show Password"]]]

     [:button {:type "submit" :hx-post "/update-password" :hx-target "#dashboard-content"} "Update Password"]]]


   [:div
    [:button {:type "submit" :hx-post "/delete-account"} "Delete Account"]
    [:p {:class "help-sign"} "Permanently deletes account and all connected transactions."]]))

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
    [:textarea {:placeholder "Your feedback..." :name "description" :class "w-full h-48 resize-none"}]
    [:button "Submit Feedback"]]))

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
