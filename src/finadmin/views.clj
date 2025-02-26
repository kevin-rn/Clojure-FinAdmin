(ns finadmin.views
  (:require
   [hiccup.page :refer [include-css include-js]]
   [hiccup2.core :as h]))




(defn sign-in []
  [:div {:id "sign-in" :class "w-full"}
   [:h1 {:class "text-center sign-title"} "Sign In"]
   [:form {:class "flex flex-col items-center"}
    [:div {:class "input-group"}
     [:span {:class "email-icon"}]
     [:input {:type "email" :required true}]
     [:label {:for ""} "Email"]]

    [:div {:class "input-group"}
     [:span {:class "password-icon"}]
     [:input {:type "password" :required true}]
     [:label {:for ""} "Password"]]

    [:button {:type "submit"} "Sign In"]

    [:div {:class "text-center switch-sign"}
     [:span "Don't have an account? "]
     [:a {:href "#" :class "sign-text" :hx-get "/sign-up" :hx-target "#auth-container"} "Sign Up"]]]])

(defn sign-up []
  [:div {:id "sign-up" :class "w-full"}
   [:h1 {:class "text-center sign-title"} "Sign Up"]
   [:form {:class "flex flex-col items-center"}
    [:div {:class "input-group"}
     [:span {:class "email-icon"}]
     [:input {:type "email" :required true}]
     [:label "Email"]]

    [:div {:class "input-group"}
     [:span {:class "password-icon"}]
     [:input {:type "password" :required true}]
     [:label "Password"]]

    [:button {:type "submit"} "Sign Up"]

    [:div {:class "text-center switch-sign"}
     [:span "Already have an account? "]
     [:a {:href "#" :class "sign-text" :hx-get "/sign-in" :hx-target "#auth-container"} "Sign In"]]]])


(defn login [view-type]
  (str "<!DOCTYPE html>"
       (h/html
        [:html
         [:head
          [:title "Clojure test"]
          (include-css "/css/output.css")
          (include-css "/css/style.css")
          (include-js "https://unpkg.com/htmx.org@2.0.4")
          [:link {:href "https://fonts.googleapis.com/css?family=Montserrat:400,900" :rel "stylesheet"}]
          [:link {:rel "icon" :href "/favicon.ico" :type "image/x-icon"}]]
         [:body {:class "bg-[url(../img/login-background.png)] login-page"}
          [:div {:class "flex h-screen"}
           [:div {:class "w-2/3 flex items-center justify-center"}
            [:div {:class "border-solid w-2/3 welcome"}
             [:h1 "Start your Financial Administration here!"]
             [:h3 "Built with Clojure"]]]
           [:div {:id "auth-container" :class "w-1/3 flex items-center justify-center sign-div"}
            (if (= view-type "sign-up")
              (sign-up) 
              (sign-in))]]]])))

(defn dashboard []
  (str "<!DOCTYPE html>"
       (h/html
        [:html
         [:head
          [:title "Clojure test"]
          (include-css "/css/output.css")
          (include-css "/css/style.css")
          (include-js "https://unpkg.com/htmx.org@2.0.4")
          [:link {:href "https://fonts.googleapis.com/css?family=Montserrat:400,900" :rel "stylesheet"}]
          [:link {:rel "icon" :href "/favicon.ico" :type "image/x-icon"}]]
         [:body
          [:div {:class "grid grid-cols-3"}
           [:div]
           [:div {:class "flex justify-center flex-col m-auto h-screen"}
            [:h1 "Dashboard"]]
           [:div]]]])))
