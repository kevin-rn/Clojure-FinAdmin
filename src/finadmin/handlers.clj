(ns finadmin.handlers
    (:require
     [finadmin.storage :as storage]
     [finadmin.views :as views]))
  
  (defn home-page [_]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (views/home storage/get-all-transactions)})
  
  (defn get-transactions [_]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (views/transactions-html storage/get-all-transactions)})
  
  (defn add-transaction [{:keys [params]}]
    (storage/add-transaction! {:id (rand-int 10000)
                          :desc (params "desc")
                          :amount (Double/parseDouble (params "amount"))})
    {:status 200
     :headers {"HX-Trigger" "update-transactions"}})