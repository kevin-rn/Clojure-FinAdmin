(ns finadmin.views.transactionsview
  (:require
   [finadmin.views.helpers :refer [approval-status currencies expense-types
                                   parse-and-format-date
                                   parse-and-format-date-input payment-methods
                                   payment-status]]
   [hiccup2.core :as h]))

(defn transactions-list
  [transactions {:keys [modal]}]
  (h/html
   [:table {:class "w-full" :id "transaction-table"}
    [:thead
     [:tr
      [:th {:class "font-semibold text-left" :onclick "sortTable(0, this)"} "Date" 
       [:span.sort-icon
        [:img {:src "/icons/dropdown.svg" :id "sort-btn-initial" :alt "Sort asc"}]]]
      [:th {:class "font-semibold text-left" :onclick "sortTable(1, this)"} "Amount" [:span.sort-icon]]
      [:th {:class "font-semibold text-left" :onclick "sortTable(2, this)"} "Currency" [:span.sort-icon]]
      [:th {:class "font-semibold text-left" :onclick "sortTable(3, this)"} "Type" [:span.sort-icon ""]]
      [:th {:class "font-semibold text-left" :onclick "sortTable(4, this)"} "Description" [:span.sort-icon]]
      [:th {:class "font-semibold text-left" :onclick "sortTable(5, this)"} "Payment method" [:span.sort-icon]]]]

    (if (empty? transactions)
      [:tbody
       [:tr {:class "text-center non-items"}
        [:td {:colspan "6"} [:i.select-none "No Transactions stored"]]]]
      [:tbody
       (for [{:transactions/keys [transaction_id transaction_date amount currency transaction_type description payment_method]} transactions]
         [:tr {:class "border-b"
               :hx-get (str "/transaction/" transaction_id "/" transaction_type)
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
   [:tr
    [:td "Expense Type"]
    [:td [:div.custom-select
          [:select {:name "expense_type"
                    :required true
                    :disabled true}
           (for [expense-type expense-types]
             [:option {:value expense-type 
                       :selected (if (= expense-type (:expenses/expense_type transaction)) "selected" nil)} expense-type])]
          [:span.custom-select-arrow]]]]
   [:tr
    [:td "Reimbursement Status"]
    [:td [:div.custom-select
          [:select {:name "reimbursement_status"
                    :required true :disabled true}
           (for [status ["Pending" "Approved" "Rejected" "Paid" "Under Review"]]
             [:option {:value status 
                       :selected (if (= status (:expenses/reimbursement_status transaction)) "selected" nil)} status])]
          [:span.custom-select-arrow]]]]
   [:tr
    [:td "Business Purpose"]
    [:td [:textarea {:name "business_purpose"
                     :class "w-full h-48 resize-none"
                     :disabled true}
          (:expenses/business_purpose transaction)]]]
   [:tr
    [:td "Approval Status"]
    [:td [:div.custom-select
          [:select {:name "approval_status"
                    :required true
                    :disabled true}
           (for [status approval-status]
             [:option {:value status 
                       :selected (if (= status (:expenses/approval_status transaction)) "selected" nil)} status])]
          [:span.custom-select-arrow]]]]
   [:tr
    [:td "Expense Date"]
    [:td [:input.w-full {:type "date"
                         :name "expense_date"
                         :required true
                         :value (parse-and-format-date-input (:expenses/expense_date transaction))
                         :disabled true}]]]))

(defn invoice-details
  [transaction]
  (h/html
   [:tr
    [:td "Invoice Number"]
    [:td [:input.w-full {:type "text"
                         :name "invoice_number"
                         :required true
                         :value (:invoices/invoice_number transaction) 
                         :disabled true}]]]
   [:tr
    [:td "Vendor Name"]
    [:td [:input.w-full {:type "text"
                         :name "vendor_name"
                         :required true
                         :value (:invoices/vendor_name transaction) 
                         :disabled true}]]]
   [:tr
    [:td "PO Number"]
    [:td [:input.w-full {:type "text"
                         :name "po_number"
                         :required true
                         :value (:invoices/po_number transaction)
                         :disabled true}]]]
   [:tr
    [:td "VAT Code"]
    [:td [:input.w-full {:type "text"
                         :name "vat_code"
                         :required true
                         :value (:invoices/vat_code transaction)
                         :disabled true}]]]
   [:tr
    [:td "Payment Terms"]
    [:td [:input.w-full {:type "text"
                         :name "payment_terms"
                         :required true
                         :value (:invoices/payment_terms transaction)
                         :disabled true}]]]
   [:tr
    [:td "Due Date"]
    [:td [:input.w-full {:type "date" 
                         :name "due_date" 
                         :required true 
                         :value (parse-and-format-date-input (:invoices/due_date transaction) )
                         :disabled true}]]]
   [:tr
    [:td "Payment Status"]
    [:td [:div.custom-select
          [:select {:name "payment_status" 
                    :required true 
                    :disabled true}
           (for [status payment-status]
             [:option {:value status 
                       :selected (if (= status (:invoices/payment_status transaction)) "selected" nil)} status])]
          [:span.custom-select-arrow]]]]))


(defn transaction-details
  [transaction {:keys [modal]}]
  (let [transaction-type (:transactions/transaction_type transaction)]
    (h/html
     [:div
      [:h2 "Transaction Details"]
      [:form
       [:table#transaction-details
        [:thead {:class "border-b-2 sticky top-0 z-2"}
         [:tr
          [:th {:class "font-semibold text-left"} "Field"]
          [:th {:class "font-semibold text-left"} "Value"]]]
        [:tbody
         [:tr
          [:td "Transaction Date"]
          [:td (parse-and-format-date (:transactions/transaction_date transaction))]]
         [:tr
          [:td "Amount"]
          [:td [:input.w-full {:type "number"
                               :name "amount"
                               :step "0.1"
                               :min "0"
                               :required true
                               :value (:transactions/amount transaction)
                               :disabled true}]]]
         [:tr
          [:td "Currency"]
          [:td [:div.custom-select
                [:select {:name "currency"
                          :required true
                          :disabled true}
                 (for [currency currencies]
                   [:option {:value currency
                             :selected (if (= currency (:transactions/currency transaction)) "selected" nil)} currency])]
                [:span.custom-select-arrow]]]]
         [:tr
          [:td "Transaction Type"]
          [:td (:transactions/transaction_type transaction)]]
         [:tr
          [:td "Description"]
          [:td [:textarea {:name "description"
                           :class "w-full h-48 resize-none"
                           :disabled true} (:transactions/description transaction)]]]
         [:tr
          [:td "Payment Method"]
          [:td  [:div.custom-select
                 [:select {:name "payment_method"
                           :required true
                           :disabled true}
                  (for [method payment-methods]
                    [:option {:value method
                              :selected (if (= method (:transactions/payment_method transaction)) "selected" nil)} method])]
                 [:span.custom-select-arrow]]]]

         (condp = (keyword transaction-type)
           :expense (expense-details transaction)
           :invoice (invoice-details transaction)
           (throw (IllegalArgumentException. (str "Invalid transaction type: " transaction-type))))]]

       [:div {:class "flex justify-between"}
        [:button {:type "button"
                  :hx-post (str "/delete-transaction/" (:transactions/transaction_id transaction) "/" transaction-type)
                  :hx-target "#transactions-container"
                  :hx-trigger "click"} (str "Delete " transaction-type) " transaction"]
        [:button {:type "submit"
                  :id "modify-transaction-btn"
                  :hx-post (str "/modify-transaction/" (:transactions/transaction_id transaction) "/" transaction-type)
                  :hx-target "#transactions-container"
                  :hx-trigger "click"
                  :disabled true} (str "Update " transaction-type) " transaction"]
        [:div {:class "checkbox"}
         [:input {:type "checkbox" :onclick "toggleEditFields(this)"}]
         [:i  "Enable Editing"]]]]

      (when modal
        [:div#popup
         [:div.backdrop]
         [:dialog {:class "popup" :open true}
          [:p "Transaction has been succesfully updated!"]
          [:button {:onclick "closeModal(this)"} "Close"]]])])))

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
      [:span.custom-select-arrow]]]

    [:div {:class "flex-1 overflow-auto mt-8" :id "transaction-list"}
     (transactions-list transactions key_map)]]))

(defn expenses-component
  [{:keys [modal]}]
  (h/html
   [:div#expense-form
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
       [:div.input-group
        [:label "Amount:"]
        [:input {:type "number" 
                 :name "amount" 
                 :step "0.1" 
                 :min "0" 
                 :required true}]]
       [:div.input-group
        [:label "Currency:"]
        [:div.custom-select
         [:select {:name "currency" 
                   :required true}
          (for [currency currencies]
            [:option {:value currency
                       :selected (if (= currency "EUR") "selected" nil)} currency])]
         [:span.custom-select-arrow]]]
       [:div.input-group
        [:label "Expense Date:"]
        [:input {:type "date" 
                 :name "expense_date" :required true}]]
       [:div {:class "input-group col-span-3"}
        [:label "Description:"]
        [:textarea {:name "description" 
                    :class "w-full h-48 resize-none"}]]]]

     ;; Payment Information
     [:fieldset {:class "col-span-3 border p-4 rounded"}
      [:legend "Payment Information"]
      [:div {:class "grid grid-cols-2 gap-4"}
       [:div.input-group
        [:label "Expense Type:"]
        [:div.custom-select
         [:select
          {:name "expense_type"
           :required true}
          (for [expense-type expense-types]
            [:option {:value expense-type
                      :selected (if (= expense-type "Operating Expenses") "selected" nil)} expense-type])]
         [:span.custom-select-arrow]]]
       [:div.input-group
        [:label "Payment Method:"]
        [:div.custom-select
         [:select {:name "payment_method" 
                   :required true}
          (for [method payment-methods]
            [:option {:value method
                      :selected (if (= method "Bank transfer") "selected" nil)} method])]
         [:span.custom-select-arrow]]]]]

     ;; Expense Classification
     [:fieldset {:class "col-span-3 border p-4 rounded"}
      [:legend "Expense Classification"]
      [:div {:class "grid grid-cols-2 gap-4"}
       [:div.input-group
        [:label "Reimbursement Status:"]
        [:div.custom-select
         [:select {:name "reimbursement_status" 
                   :required true}
          (for [status ["Pending" "Approved" "Rejected" "Paid" "Under Review"]]
            [:option {:value status 
                      :selected (if (= status "Pending") "selected" nil)} status])]
         [:span.custom-select-arrow]]]
       [:div.input-group
        [:label "Approval Status:"]
        [:div.custom-select
         [:select {:name "approval_status" 
                   :required true}
          (for [status approval-status]
            [:option {:value status 
                      :selected (if (= status "Pending") "selected" nil)} status])]
         [:span.custom-select-arrow]]]
       [:div {:class "input-group col-span-2"}
        [:label "Business Purpose:"]
        [:textarea {:name "business_purpose" 
                    :class "w-full h-48 resize-none"}]]]]

     [:button {:type "submit"} "Submit"]]]))

(defn invoices-component
  [{:keys [modal]}]
  (h/html
   [:div#invoice-form
    [:h2 "Add Invoice"]
    (when modal
      [:div#popup
       [:div.backdrop]
       [:dialog {:class "popup" 
                 :open true}
        [:p "Invoice has been registered succesfully!"]
        [:button {:onclick "closeModal(this)"} "Close"]]]) 

    [:form {:class "grid grid-cols-3 gap-4"
            :hx-post "/add-invoice"
            :hx-target "#invoice-form"}

     ;; Invoice Details
     [:fieldset {:class "col-span-3 border p-4 rounded"}
      [:legend "Invoice Details"]
      [:div {:class "grid grid-cols-3 gap-4"}
       [:div.input-group
        [:label "Invoice Number:"]
        [:input {:type "text"
                 :name "invoice_number"
                 :required true}]]
       [:div.input-group
        [:label "Amount:"]
        [:input {:type "number"
                 :name "amount"
                 :step "0.1"
                 :min "0"
                 :required true}]]
       [:div.input-group
        [:label "Currency:"]
        [:div.custom-select
         [:select {:name "currency"
                   :required true}
          (for [currency currencies]
            [:option {:value currency
                      :selected (if (= currency "EUR") "selected" nil)} currency])]
         [:span.custom-select-arrow]]]
       [:div {:class "input-group col-span-3"}
        [:label "Description:"]
        [:textarea {:name "description"
                    :class "w-full h-48 resize-none"}]]]]

     ;; Vendor Information
     [:fieldset {:class "col-span-3 border p-4 rounded"}
      [:legend "Vendor Information:"]
      [:div {:class "grid grid-cols-3 gap-4"}
       [:div.input-group
        [:label "Vendor Name:"]
        [:input {:type "text"
                 :name "vendor_name"
                 :required true}]]
       [:div.input-group
        [:label "PO Number:"]
        [:input {:type "text"
                 :name "po_number"
                 :required true}]]
       [:div.input-group
        [:label "VAT code:"]
        [:input {:type "text"
                 :name "vat_code"
                 :required true}]]]]

     ;; Payment Information
     [:fieldset {:class "col-span-3 border p-4 rounded"}
      [:legend "Payment Information"]
      [:div {:class "grid grid-cols-2 gap-4"}
       [:div.input-group
        [:label.col-span-2 "Payment Method:"]
        [:div.custom-select
         [:select {:name "payment_method"
                   :required true}
          (for [method payment-methods]
            [:option {:value method
                      :selected (if (= method "Bank transfer") "selected" nil)} method])]
         [:span.custom-select-arrow]]]
       [:div.input-group
        [:label "Payment Terms:"]
        [:input {:type "text" 
                 :name "payment_terms" 
                 :required true}]]
       [:div.input-group
        [:label "Due Date:"]
        [:input {:type "date" 
                 :name "due_date" 
                 :required true}]]
       [:div.input-group
        [:label "Payment Status:"]
        [:div.custom-select
         [:select {:name "payment_status"
                   :required true}
          (for [status payment-status]
            [:option {:value status 
                      :selected (if (= status "Unpaid") "selected" nil)} status])]
         [:span.custom-select-arrow]]]]]
     [:button {:type "submit"} "Submit"]]]))