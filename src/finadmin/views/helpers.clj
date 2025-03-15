(ns finadmin.views.helpers
  (:require
   [clojure.data.json :as json]) 
  (:import
   [java.sql Timestamp]
   [java.time.format DateTimeFormatter]
   [java.util Locale]))

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

(defn parse-and-format-date [datetime]
  (if (instance? Timestamp datetime)
    (let [output-formatter (.withLocale (DateTimeFormatter/ofPattern "d MMMM yyyy - HH:mm:ss") Locale/ENGLISH)
          local-datetime (.toLocalDateTime datetime)]
      (.format local-datetime output-formatter))
    (throw (IllegalArgumentException. "Expected a java.sql.Timestamp object"))))

(defn parse-and-format-date-input
  " ISO 8601 format (yyyy-MM-dd), "
  [datetime]
  (if (instance? Timestamp datetime)
    (let [output-formatter (.withLocale (DateTimeFormatter/ofPattern "yyyy-MM-dd") Locale/ENGLISH)
          local-datetime (.toLocalDateTime datetime)]
      (.format local-datetime output-formatter))
    (throw (IllegalArgumentException. "Expected a java.sql.Timestamp object"))))

(defn count-by-key
  [key data]
  (json/write-str
   (reduce (fn [acc item]
             (let [value (key item)]
               (update acc value (fnil inc 0))))
           {}
           data)))

(defn total-amount-per-currency [records]
 (->> records
      (group-by :transactions/currency)
      (map (fn [[currency records]]
             [currency (reduce + (map :transactions/amount records))]))))

(defn extract-year-month
  [datetime]
  (if (instance? Timestamp datetime)
    (let [local-datetime (.toLocalDateTime datetime)
          year (.getYear local-datetime)
          month (.getMonthValue local-datetime)]
      {:year year
       :month month})
    (throw (IllegalArgumentException. "Expected a java.sql.Timestamp object"))))

(defn group-by-month-and-currency [transactions]
  (->> transactions
       (map #(update % :transactions/transaction_date extract-year-month))
       (group-by #(vector (:transactions/currency %) (:transactions/transaction_date %)))))

(defn aggregate-transactions [grouped-transactions]
  (let [months (range 1 13)]
    (reduce (fn [result [currency-month transactions]]
              (let [[currency {}] currency-month
                    ;; Separate expenses and invoices
                    expenses (filter #(contains? % :expenses/expense_id) transactions)
                    invoices (filter #(contains? % :invoices/invoice_id) transactions)

                    ;; Function to aggregate by month
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
                                     :stack "expense"
                                     :backgroundColor "red"})

                    invoice-entry (when (seq invoices)
                                    {:label (str currency " Invoices")
                                     :data (map (aggregate-by-month invoices) months)
                                     :stack "invoice"
                                     :backgroundColor "blue"})]

                ;; Add non-nil entries to the result
                (cond-> result
                  expense-entry (conj expense-entry)
                  invoice-entry (conj invoice-entry))))
            []
            grouped-transactions)))
