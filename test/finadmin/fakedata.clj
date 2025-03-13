(ns finadmin.fakedata
  (:require
   [finadmin.database.transactiondb :refer [add-expense-db add-invoice-db]]
   [finadmin.views.transactionsview :refer [currencies expense-types
                                            payment-methods]])
   (:import
    [java.time LocalDate]
    [java.util Random]))

;; Random helpers
(defn rand-item [items]
  (rand-nth items))

(defn random-amount []
  (* (rand) 10000))

(defn random-date []
  (let [random (Random.)
        year (+ 1950 (.nextInt random 100))
        month (+ 1 (.nextInt random 12))
        day (+ 1 (.nextInt random 28))
        random-date (LocalDate/of year month day)]
    (.toString random-date)))


;; Generate fake expense data
(defn generate-fake-expense []
  {:amount (random-amount)
   :currency (rand-item currencies)
   :description (str "Fake description for expense #" (rand-int 1000))
   :expense_date (random-date)
   :expense_type (rand-item expense-types)
   :payment_method (rand-item payment-methods)
   :reimbursement_status (rand-item ["Pending" "Approved" "Rejected"])
   :approval_status (rand-item ["Pending" "Approved" "Denied"])
   :business_purpose (str "Fake business purpose #" (rand-int 1000))})

;; Generate fake invoice data
(defn generate-fake-invoice []
  {:amount (random-amount)
   :currency (rand-item currencies)
   :description (str "Fake description for invoice #" (rand-int 1000))
   :invoice_number (str "INV-" (rand-int 10000))
   :vendor_name (str "Fake Vendor #" (rand-int 100))
   :po_number (str "PO-" (rand-int 100000))
   :vat_code (rand-item ["VAT1" "VAT2" "VAT3" "VAT4"])
   :payment_method (rand-item payment-methods)
   :payment_terms (rand-item ["Net 30" "Net 60" "Due on receipt"])
   :due_date (random-date)
   :payment_status (rand-item ["Unpaid" "Paid" "Partial payment" "Overdue"])})

;; Helper function to simulate adding multiple fake expense transactions
(defn add-fake-expenses
  [n email]
  (dotimes [_ n]
    (let [fake-expense (generate-fake-expense)]
      (add-expense-db email fake-expense))))

;; Helper function to simulate adding multiple fake invoice transactions
(defn add-fake-invoices
  [n email]
  (dotimes [_ n]
    (let [fake-invoice (generate-fake-invoice)]
      (add-invoice-db email fake-invoice))))

(add-fake-expenses 10 "test@mail.com")
(add-fake-invoices 10 "test@mail.com")

