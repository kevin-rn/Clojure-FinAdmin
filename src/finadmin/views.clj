(ns finadmin.views
  (:require
   [hiccup.core :refer [html]]))

(defn transactions-html [transactions]
  (html
   [:table
    [:tr [:th "ID"] [:th "Description"] [:th "Amount"]]
    (for [{:keys [id desc amount]} transactions]
      [:tr [:td id] [:td desc] [:td (str "$" amount)]] )]))

(defn home [ transactions ]
  (html
   [:html
    [:head
     [:script {:src "/htmx.min.js"}]]
    [:body
     [:h1 "Welcome to Clojure Financial Admin"]
     [:p "This is a dummy application for performing Financial administration."]
     [:form {:hx-post "/submit"
             :hx-trigger "submit"
             :hx-target "#response"
             :hx-swap "innerHTML"}
      [:input {:type "text" :name "input" :placeholder "Enter something"}]
      [:button "Submit"]]
     [:div {:id "response"}]]]))

