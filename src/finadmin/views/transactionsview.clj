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