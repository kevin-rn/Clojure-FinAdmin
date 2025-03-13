(ns finadmin.views.transactionsview
  (:require
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

(defn transactions-list
  [transactions {:keys [modal]}]
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
        [:td {:colspan "6"} [:i.select-none "No Transactions stored"]]]]
      [:tbody
       (for [{:transactions/keys [transaction_id transaction_date amount currency transaction_type description payment_method]} transactions]
         [:tr {:class "border-b"
               :hx-get (str "/transaction/" transaction_id "/type/" transaction_type)
               :hx-target "#transactions-container"
               :hx-trigger "click"}
          [:td (parse-and-format-date transaction_date)]
          [:td amount]
          [:td currency]
          [:td transaction_type]
          [:td description]
          [:td payment_method]])])]

   (when modal
     [:div#popup
      [:div.backdrop]
      [:dialog {:class "popup" :open true}
       [:p "Transaction has been deleted succesfully!"]
       [:button {:onclick "closeModal(this)"} "Close"]]])))

(defn expense-details 
  [transaction]
  (h/html
   [:tr [:td "Expense Type"] [:td (:expenses/expense_type transaction)]]
   [:tr [:td "Reimbursement Status"] [:td (:expenses/reimbursement_status transaction)]]
   [:tr [:td "Business Purpose"] [:td (:expenses/business_purpose transaction)]]
   [:tr [:td "Approval Status"] [:td (:expenses/approval_status transaction)]]
   [:tr [:td "Expense Date"] [:td (:expenses/expense_date transaction)]]))

(defn invoice-details 
  [transaction]
  (h/html
   [:tr [:td "Invoice Number"] [:td (:invoices/invoice_number transaction)]]
   [:tr [:td "Vendor Name"] [:td (:invoices/vendor_name transaction)]]
   [:tr [:td "PO Number"] [:td (:invoices/po_number transaction)]]
   [:tr [:td "VAT Code"] [:td (:invoices/vat_code transaction)]]
   [:tr [:td "Payment Terms"] [:td (:invoices/payment_terms transaction)]]
   [:tr [:td "Due Date"] [:td (:invoices/due_date transaction)]]
   [:tr [:td "Payment Status"] [:td (:invoices/payment_status transaction)]]))

(defn transaction-details
  [transaction]
  (let [transaction-type (:transactions/transaction_type transaction)]
    (h/html
     [:div
      [:h2 "Transaction Details"]
      [:table
       [:tr [:th "Field"] [:th "Value"]]
       [:tr [:td "Date"] [:td (:transactions/transaction_date transaction)]]
       [:tr [:td "Amount"] [:td (:transactions/amount transaction)]]
       [:tr [:td "Currency"] [:td (:transactions/currency transaction)]]
       [:tr [:td "Type"] [:td (:transactions/transaction_type transaction)]]
       [:tr [:td "Description"] [:td (:transactions/description transaction)]]
       [:tr [:td "Payment Method"] [:td (:transactions/payment_method transaction)]]
       (condp = (keyword transaction-type)
         :expense (expense-details transaction)
         :invoice (invoice-details transaction)
         (throw (IllegalArgumentException. (str "Invalid transaction type: " transaction-type))))]

      [:div
       [:button {:type "button"
                 :hx-get (str "/delete-transaction/" (:transactions/transaction_id transaction) "/type/" transaction-type)
                 :hx-target "#transactions-container"
                 :hx-trigger "click"} (str "Delete " transaction-type) " transaction"]]])))

(defn transactions-component
  [transactions key_map]
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
     (transactions-list transactions key_map)]]))

(defn expenses-component
  [{:keys [modal]}]
  (h/html
   [:div {:id "expense-form"}
    [:h2 "Add Expense"]
    (when modal
      [:div#popup
       [:div.backdrop]
       [:dialog {:class "popup" :open true}
        [:p "Expense has been registered succesfully!"]
        [:button {:onclick "closeModal(this)"} "Close"]]])
    [:form {:class "grid grid-cols-3 gap-4"
            :hx-post "/add-expense"
            :hx-target "#expense-form"}

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
      [:div#popup
       [:div.backdrop]
       [:dialog {:class "popup" :open true}
        [:p "Invoice has been registered succesfully!"]
        [:button {:onclick "closeModal(this)"} "Close"]]])
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