(ns finadmin.views
  (:require
   [hiccup.page :refer [include-css include-js]]
   [hiccup2.core :as h])
  (:import
   [java.sql Timestamp]
   [java.time.format DateTimeFormatter]
   [java.util Locale]))

(def currencies ["ARS" "AUD" "BHD" "BRL" "CAD" "CHF"
                 "CNY" "COP" "CZK" "DKK" "EGP" "EUR"
                 "GBP" "HKD" "ILS" "INR" "IDR" "JPY"
                 "KRW" "KWD" "MAD" "MXN" "MYR" "NOK"
                 "PEN" "PHP" "PLN" "RUB" "SAR" "SEK"
                 "SGD" "THB" "TRY" "USD" "VND" "ZAR" "NZD"])

(def expense-types ["Fixed Expenses" "Variable Expenses" "Operating Expenses"
                    "Capital Expenses" "Interest Expenses" "Depreciation Expenses"
                    "Cost of Goods Sold" "Non-operating Expenses" "Miscellaneous Expenses"])

(def payment-methods ["Credit card" "Debit card" "Online payment" 
                      "Bank transfer" "BNPL" "Cash" "Check" 
                      "Crypto" "Mobile payment" "Other"])

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
     [:label {:for "transaction-type"} "Transaction Type: "]
     [:div.custom-select
      [:select {:name "transaction-type" 
                :class "w-auto min-w-max"
                :hx-get "/filter-transactions" 
                :hx-target "#transaction-list" 
                :hx-trigger "change"}
       [:option {:value "all"} "All"]
       [:option {:value "invoice"} "Invoices"]
       [:option {:value "expense"} "Expenses"]]
      [:span {:class "custom-select-arrow"}]]]

    [:div {:class "flex-1 overflow-auto mt-8" :id "transaction-list"}
     (transactions-list transactions)]]))

(defn expenses-component
  [{:keys [modal]}]
  (h/html
   [:div {:id "expense-form"}
    [:h2 "Add Expense"]
    (when modal
      [:dialog {:class "popup" :open true}
       [:p "Invoice has been registered succesfully!"]
       [:button {:onclick "closeModal(this)"} "Close"]])
    [:form {:class "grid grid-cols-3 gap-4"
            :hx-post "/add-expense"}

     ;; Expense Details
     [:fieldset {:class "col-span-3 border p-4 rounded"}
      [:legend "Expense Details"]
      [:div {:class "grid grid-cols-3 gap-4"}
       [:div {:class "input-group"}
        [:label "Amount:"]
        [:input {:type "number" :name "amount" :step "0.1" :min "0" :required true}]]
       [:div {:class "input-group"}
        [:label "Currency:"]
        [:div.custom-select
         [:select {:name "currency" :required true}
          (for [currency currencies]
            [:option {:value currency :selected (if (= currency "EUR") "selected" nil)} currency])]
         [:span {:class "custom-select-arrow"}]]]
       [:div {:class "input-group"}
        [:label "Expense Date:"]
        [:input {:type "date" :name "expense_date" :required true}]]
       [:div {:class "input-group col-span-3"}
        [:label "Description:"]
        [:textarea {:name "description" :class "w-full h-48 resize-none"}]]]]

     ;; Payment Information
     [:fieldset {:class "col-span-3 border p-4 rounded"}
      [:legend "Payment Information"]
      [:div {:class "grid grid-cols-2 gap-4"}
       [:div {:class "input-group"}
        [:label "Expense Type:"]
        [:div.custom-select
         [:select {:name "expense_type" :required true}
          (for [expense-type expense-types]
            [:option {:value expense-type :selected (if (= expense-type "Operating Expenses") "selected" nil)} expense-type])]
         [:span {:class "custom-select-arrow"}]]]
       [:div {:class "input-group"}
        [:label "Payment Method:"]
        [:div.custom-select
         [:select {:name "payment_method" :required true}
          (for [method payment-methods]
            [:option {:value method :selected (if (= method "Bank transfer") "selected" nil)} method])]
         [:span {:class "custom-select-arrow"}]]]]]

     ;; Expense Classification
     [:fieldset {:class "col-span-3 border p-4 rounded"}
      [:legend "Expense Classification"]
      [:div {:class "grid grid-cols-2 gap-4"}
       [:div {:class "input-group"}
        [:label "Reimbursement Status:"]
        [:div.custom-select
         [:select {:name "reimbursement_status" :required true}
          (for [status ["Pending" "Approved" "Rejected" "Paid" "Under Review"]]
            [:option {:value status :selected (if (= status "Pending") "selected" nil)} status])]
         [:span {:class "custom-select-arrow"}]]]
       [:div {:class "input-group"}
        [:label "Approval Status:"]
        [:div.custom-select
         [:select {:name "approval_status" :required true}
          (for [status ["Pending" "Approved" "Rejected" "In Progress" "On Hold" "Completed" "Needs Revision" "Escalated"]]
            [:option {:value status :selected (if (= status "Pending") "selected" nil)} status])]
         [:span {:class "custom-select-arrow"}]]]
       [:div {:class "input-group col-span-2"}
        [:label "Business Purpose:"]
        [:textarea {:name "business_purpose" :class "w-full h-48 resize-none"}]]]]

     [:button {:type "submit"} "Submit"]]]))

(defn invoices-component
  [{:keys [modal]}]
  (h/html
   [:div {:id "invoice-form"}
    [:h2 "Add Invoice"] 
    (when modal
      [:dialog {:class "popup" :open true}
       [:p "Invoice has been registered succesfully!"]
       [:button {:onclick "closeModal(this)"} "Close"]])
    [:form {:class "grid grid-cols-3 gap-4"
            :hx-post "/add-invoice"
            :hx-target "#invoice-form"}

     ;; Invoice Details
     [:fieldset {:class "col-span-3 border p-4 rounded"}
      [:legend "Invoice Details"]
      [:div {:class "grid grid-cols-3 gap-4"}
       [:div {:class "input-group"}
        [:label "Invoice Number:"]
        [:input {:type "text" :name "invoice_number" :required true}]]
       [:div {:class "input-group"}
        [:label "Amount:"]
        [:input {:type "number" :name "amount" :step "0.1" :min "0" :required true}]]
       [:div {:class "input-group"}
        [:label "Currency:"]
        [:div.custom-select
         [:select {:name "currency" :required true}
          (for [currency currencies]
            [:option {:value currency :selected (if (= currency "EUR") "selected" nil)} currency])]
         [:span {:class "custom-select-arrow"}]]]
       [:div {:class "input-group col-span-3"}
        [:label "Description:"]
        [:textarea {:name "description" :class "w-full h-48 resize-none"}]]]]

     ;; Vendor Information
     [:fieldset {:class "col-span-3 border p-4 rounded"}
      [:legend "Vendor Information:"]
      [:div {:class "grid grid-cols-3 gap-4"}
       [:div {:class "input-group"}
        [:label "Vendor Name:"]
        [:input {:type "text" :name "vendor_name" :required true}]]
       [:div {:class "input-group"}
        [:label "PO Number:"]
        [:input {:type "text" :name "po_number" :required true}]]
       [:div {:class "input-group"}
        [:label "VAT code:"]
        [:input {:type "text" :name "vat_code" :required true}]]]]

     ;; Payment Information
     [:fieldset {:class "col-span-3 border p-4 rounded"}
      [:legend "Payment Information"]
      [:div {:class "grid grid-cols-2 gap-4"}
       [:div {:class "input-group"}
        [:label {:class "col-span-2"} "Payment Method:"]
        [:div.custom-select
         [:select {:name "payment_method" :required true}
          (for [method payment-methods]
            [:option {:value method :selected (if (= method "Bank transfer") "selected" nil)} method])]
         [:span {:class "custom-select-arrow"}]]]
       [:div {:class "input-group"}
        [:label "Payment Terms:"]
        [:input {:type "text" :name "payment_terms" :required true}]]
       [:div {:class "input-group"}
        [:label "Due Date:"]
        [:input {:type "date" :name "due_date" :required true}]]
       [:div {:class "input-group"}
        [:label "Payment Status:"]
        [:div.custom-select
         [:select {:name "payment_status" :required true}
          (for [status ["Unpaid" "Paid" "Partial payment" "Overdue" "Pending"
                        "Failed" "Canceled" "Refunded"]]
            [:option {:value status :selected (if (= status "Unpaid") "selected" nil)} status])]
         [:span {:class "custom-select-arrow"}]]]]]
     [:button {:type "submit"} "Submit"]]]))

(defn settings-component
  [account {:keys [error modal]}]
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
      [:div {:class "error-message flex items-center"}
       [:img {:src "/icons/error.svg" :alt "Error message"}]
       [:p error]])

    [:form {:id "password-reset"}
     [:div {:class "input-group grid grid-cols-6 grid-rows-1 gap-4"}
      [:label {:for "current-password"} "Current Password"]
      [:input {:class "password" :type "password" :name "current-password" :value ""}]
      [:input {:type "checkbox" :id "current-visibility" :class "visibility toggle-password" :onclick "togglePassword(this)"}]
      [:label {:for "current-visibility" :class "visibility-icon hidden"}]]
     [:div {:class "input-group grid grid-cols-6 grid-rows-1 gap-4"}
      [:label {:for "new-password"} "New Password"]
      [:input {:class "password" :type "password" :name "new-password" :value ""}]
      [:input {:type "checkbox" :id "new-visibility" :class "visibility toggle-password" :onclick "togglePassword(this)"}]
      [:label {:for "new-visibility" :class "visibility-icon hidden"}]]
     [:div {:class "input-group grid grid-cols-6 grid-rows-1 gap-4"}
      [:label {:for "verify-password"} "Verify New Password"]
      [:input {:class "password" :type "password" :name "verify-password" :value ""}]
      [:input {:type "checkbox" :id "verify-visibility" :class "visibility toggle-password" :onclick "togglePassword(this)"}]
      [:label {:for "verify-visibility" :class "visibility-icon hidden"}]]
     [:button {:type "submit"
               :hx-post "/update-password"
               :hx-target "#dashboard-content"} "Update Password"]]]
   (when modal
     [:dialog {:class "popup" :open true}
      [:p "Your password has been updated succesfully"]
      [:button {:onclick "closeModal(this)"} "Close"]])

   [:div {:id "warning-sign"}
    [:div
     [:p "Are you sure you want to delete your account?"]
     [:p "This action is permanent and cannot be undone."]
     [:p "Deletes all user data, i.e. user account and all connected transactions."]]
    [:div {:class "checkbox"}
     [:input {:type "checkbox" :id "toggleAcknowledgement" :onclick "toggleAcknowledge(this)"}]
     [:i  "I acknowledge this decision."]]
    [:button {:type "submit" :id "delete-account-btn" :hx-post "/delete-account" :disabled true} "Delete Account"]]))

(defn support-component
  []
  (h/html
   [:div {:id "faq-section"}
    [:h2 "Frequently Asked Questions"]
    [:ul
     [:li
      [:p "What features does this app have?"]
      [:p "In this amazing webapp, you can add, edit, and delete your financial transactions."]]
     [:li
      [:p "What type of financial transactions are supported?"]
      [:p "Only invoices and expenses are supported."]]
     [:li
      [:p "How do I reset my password?"]
      [:p "Go to the settings tab, and follow the steps at the bottom to update your password."]]
     [:li
      [:p "Will this webapp have more features?"]
      [:p "No, we currently do not plan to add additional features."]]
     [:li
      [:p "Can I export my financial data?"]
      [:p "No, the webapp doesn't have any export functionality."]]
     [:li
      [:p "Can I use the webapp on mobile devices?"]
      [:p "No, this webapp has been designed for 1920x1080 desktop viewport."]]
     [:li
      [:p "What should I do if I encounter an error?"]
      [:p "If you experience any issues, raise an issue on github: "] 
      [:a {:href "https://github.com/kevin-rn/Clojure-FinAdmin"} "https://github.com/kevin-rn/Clojure-FinAdmin"]]
     [:li
      [:p "Do I need an account to use the webapp?"]
      [:p "Yes, otherwise you wouldn't be able to access the dashboard and see this page."]]]]


   [:div
    [:h2 "Feedback"]
    [:p "We value your feedback. Please let us know how we can improve."]
    [:textarea {:placeholder "Your feedback..." :name "description" :class "w-full h-48 resize-none"}]
    [:button {:type "submit" :disabled true} "Submit Feedback"]
    [:div {:id "warning-sign"} [:p  "*This feature has not been implemented."]]]))

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
