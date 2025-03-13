(ns finadmin.views.helpers
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
