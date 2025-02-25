(ns finadmin.storage)

(defonce transactions (atom []))

(defn get-all-transactions []
  (let [all-transactions @transactions]
    (println "Transactions: " all-transactions))
  @transactions)

(defn add-transaction! [tx]
  (swap! transactions conj tx))


(defn get-dummy-transactions []
  [{:id 1 :desc "Coffee" :amount 4.5}
   {:id 2 :desc "Lunch" :amount 12.0}
   {:id 3 :desc "Groceries" :amount 45.0}
   {:id 4 :desc "Book" :amount 15.5}
   {:id 5 :desc "Movie" :amount 20.0}])

(defn load-dummy-data! []
  (reset! transactions (get-dummy-transactions)))

(load-dummy-data!)