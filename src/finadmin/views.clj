(ns finadmin.views
  (:require
   [hiccup.page :refer [include-css]]
   [hiccup2.core :as h]))

(defn transactions-html [transactions]
  (h/html
   [:table
    [:tr [:th "ID"] [:th "Description"] [:th "Amount"]]
    (for [{:keys [id desc amount]} transactions]
      [:tr [:td id] [:td desc] [:td (str "$" amount)]] )]))

(defn home [transactions]
  (str
    (h/html
     [:html
      [:head
       [:title "Clojure test"]
       (include-css "/css/output.css")
       [:link {:rel "icon" :href "/favicon.ico" :type "image/x-icon"}]]
      [:body
       [:div {:class "flex justify-center flex-col m-auto h-screen"}
        [:h1 "Welcome to Test Dummy App"]
        [:p "This is a dummy application for performing dummy actions."]]]])))