(ns finadmin.views.helpers
  (:require
   [clojure.data.json :as json]) 
  (:import
   [java.sql Timestamp]
   [java.time.format DateTimeFormatter]
   [java.util Locale]))


;; Form Select options
(def approval-status ["Pending" "Approved" "Rejected" "In Progress"
                      "On Hold" "Completed" "Needs Revision" "Escalated"])

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

(def payment-status ["Unpaid" "Paid" "Partial payment" "Overdue" "Pending"
                     "Failed" "Canceled" "Refunded"])

(def document-types ["Invoice", "Payment Receipt", "Bank Statement",
                     "Payroll Report", "Tax Invoice", "Tax Return",
                     "Balance Sheet", "Profit & Loss Statement",
                     "General Ledger Report", "Purchase Order"])

;; Helper methods
(defn modal-component 
  "Renders a modal popup with a message if `modal` is not nil.
  
       Parameters:
       - `modal`: A string representing the message to display in the modal.
  
       Returns:
       - A modal popup with the provided message if `modal` is not nil."
  [modal]
  (when modal
    [:div#popup
     [:div.backdrop]
     [:dialog {:class "popup" :open true}
      [:p modal]
      [:button {:onclick "closeModal(this)"} "Close"]]]))


(defn parse-and-format-date 
  "Parses and formats a Timestamp object into a human-readable date string.
  
       Parameters:
       - `datetime`: A java.sql.Timestamp object representing a date and time.
  
       Returns:
       - A formatted string representing the date and time in the format 'd MMMM yyyy - HH:mm:ss'.
       
       Throws:
       - IllegalArgumentException if the input is not a Timestamp."
  [datetime]
  (if (instance? Timestamp datetime)
    (let [output-formatter (.withLocale (DateTimeFormatter/ofPattern "d MMMM yyyy - HH:mm:ss") Locale/ENGLISH)
          local-datetime (.toLocalDateTime datetime)]
      (.format local-datetime output-formatter))
    (throw (IllegalArgumentException. "Expected a java.sql.Timestamp object"))))


(defn parse-and-format-date-input
  "Parses and formats a Timestamp object into an ISO 8601 date string (yyyy-MM-dd).

     Parameters:
     - `datetime`: A java.sql.Timestamp object representing a date.

     Returns:
     - A formatted string representing the date in ISO 8601 format 'yyyy-MM-dd'.
     
     Throws:
     - IllegalArgumentException if the input is not a Timestamp."
  [datetime]
  (if (instance? Timestamp datetime)
    (let [output-formatter (.withLocale (DateTimeFormatter/ofPattern "yyyy-MM-dd") Locale/ENGLISH)
          local-datetime (.toLocalDateTime datetime)]
      (.format local-datetime output-formatter))
    (throw (IllegalArgumentException. "Expected a java.sql.Timestamp object"))))


(defn count-by-key
  "Counts occurrences of each value in a collection of maps based on a given key.
  
       Parameters:
       - `key`: A function that extracts the key from each item in the collection.
       - `data`: A collection of maps or items to be counted.
  
       Returns:
       - A JSON string representing the count of each value corresponding to the provided key."
  [key data]
  (json/write-str
   (reduce (fn [acc item]
             (let [value (key item)]
               (update acc value (fnil inc 0))))
           {}
           data)))


(defn total-amount-per-currency
  "Calculates the total amount for each currency in a list of records.
  
       Parameters:
       - `records`: A collection of transaction records, each containing a currency and amount.
  
       Returns:
       - A sequence of pairs where each pair consists of a currency and its total amount."
  [records]
  (->> records
       (group-by :transactions/currency)
       (map (fn [[currency records]]
              [currency (reduce + (map :transactions/amount records))]))))


(defn extract-year-month
  "Extracts the year and month from a Timestamp object.
  
       Parameters:
       - `datetime`: A java.sql.Timestamp object representing a date and time.
  
       Returns:
       - A map containing the year and month of the given datetime.
  
       Throws:
       - IllegalArgumentException if the input is not a Timestamp."
  [datetime]
  (if (instance? Timestamp datetime)
    (let [local-datetime (.toLocalDateTime datetime)
          year (.getYear local-datetime)
          month (.getMonthValue local-datetime)]
      {:year year
       :month month})
    (throw (IllegalArgumentException. "Expected a java.sql.Timestamp object"))))


(defn group-by-month-and-currency
  "Groups a list of transactions by month and currency.
  
       Parameters:
       - `transactions`: A list of transaction records, each containing a currency and transaction date.
  
       Returns:
       - A map where the keys are vectors of [currency, year-month], and the values are lists of transactions."
  [transactions]
  (->> transactions
       (map #(update % :transactions/transaction_date extract-year-month))
       (group-by #(vector (:transactions/currency %) (:transactions/transaction_date %)))))


(defn aggregate-transactions 
  "Aggregates transactions by month and currency, separating expenses and invoices.
  
       Parameters:
       - `grouped-transactions`: A map of transactions grouped by currency and month.
  
       Returns:
       - A list of aggregated data entries for expenses and invoices, with monthly totals for each."
  [grouped-transactions]
  (let [months (range 1 13)]
    (reduce (fn [result [currency-month transactions]]
              (let [[currency {}] currency-month
                    expenses (filter #(contains? % :expenses/expense_id) transactions)
                    invoices (filter #(contains? % :invoices/invoice_id) transactions)
                    aggregate-by-month (fn [txns]
                                         (reduce (fn [acc month]
                                                   (let [month-transactions (->> txns
                                                                                 (filter #(= month (:month (:transactions/transaction_date %))))
                                                                                 (map #(double (:transactions/amount %))))]
                                                     (assoc acc month (reduce + 0.0 month-transactions))))
                                                 (zipmap months (repeat 0.0))
                                                 months))
                    expense-entry (when (seq expenses)
                                    {:label (str currency " Expenses")
                                     :data (map (aggregate-by-month expenses) months)
                                     :stack "expense"})

                    invoice-entry (when (seq invoices)
                                    {:label (str currency " Invoices")
                                     :data (map (aggregate-by-month invoices) months)
                                     :stack "invoice"})]
                (cond-> result
                  expense-entry (conj expense-entry)
                  invoice-entry (conj invoice-entry))))
            []
            grouped-transactions)))
