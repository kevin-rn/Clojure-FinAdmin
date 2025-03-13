(ns finadmin.views.dashboardview
    (:require
     [finadmin.views.helpers :refer [parse-and-format-date]]
     [hiccup.page :refer [include-css include-js]]
     [hiccup2.core :as h]))


(defn overview-component
  []
  (h/html
   [:div
    [:p "overview"]]))

(defn forms-component
  []
  (h/html
   [:div
    [:h2 "Upload Forms or Documents"]
    [:button "Upload"]]

   [:div
    [:h2 "Documents:"]
    [:ul
     [:li "Some documents"]]]))


(defn settings-component
  [account {:keys [error modal]}]
  (h/html
   [:div#settings-form
    [:h2 "Profile"]
    [:div {:class "grid grid-cols-6 grid-rows-1 gap-4"}
     [:div {:class "col-span-1"}
      [:p "Email:"]
      [:p "Password:"]
      [:p "Created at:"]]

     [:div {:class "col-span-3"}
      [:p [:i (:accounts/email account)]]
      [:p [:i (:accounts/password account)]]
      [:p [:i (parse-and-format-date (:accounts/created_at account))]]]]]

   [:div
    [:h2 "Update Password"]
    (when error
      [:div {:class "error-message flex items-center"}
       [:img {:src "/icons/error.svg" :alt "Error message"}]
       [:p error]])

    [:form {:id "password-reset"}
     [:div {:class "input-group grid grid-cols-6 grid-rows-1 gap-4"}
      [:label {:for "current-password"} "Current Password"]
      [:input {:class "password" :type "password" :name "current-password" :value ""}]
      [:input {:type "checkbox" :id "current-visibility" :class "visibility toggle-password" :onclick "togglePassword(this)"}]
      [:label {:for "current-visibility" :class "visibility-icon hidden"}]]
     [:div {:class "input-group grid grid-cols-6 grid-rows-1 gap-4"}
      [:label {:for "new-password"} "New Password"]
      [:input {:class "password" :type "password" :name "new-password" :value ""}]
      [:input {:type "checkbox" :id "new-visibility" :class "visibility toggle-password" :onclick "togglePassword(this)"}]
      [:label {:for "new-visibility" :class "visibility-icon hidden"}]]
     [:div {:class "input-group grid grid-cols-6 grid-rows-1 gap-4"}
      [:label {:for "verify-password"} "Verify New Password"]
      [:input {:class "password" :type "password" :name "verify-password" :value ""}]
      [:input {:type "checkbox" :id "verify-visibility" :class "visibility toggle-password" :onclick "togglePassword(this)"}]
      [:label {:for "verify-visibility" :class "visibility-icon hidden"}]]
     [:button {:type "submit"
               :hx-post "/update-password"
               :hx-target "#dashboard-content"} "Update Password"]]]

   (when modal
     [:div#popup
      [:div.backdrop]
      [:dialog {:class "popup" :open true}
       [:p "Your password has been updated succesfully!"]
       [:button {:onclick "closeModal(this)"} "Close"]]])

   [:div {:id "warning-sign"}
    [:div
     [:p "Are you sure you want to delete your account?"]
     [:p "This action is permanent and cannot be undone."]
     [:p "Deletes all user data, i.e. user account and all connected transactions."]]
    [:div {:class "checkbox"}
     [:input {:type "checkbox" :id "toggleAcknowledgement" :onclick "toggleAcknowledge(this)"}]
     [:i  "I acknowledge this decision."]]
    [:button {:type "submit" :id "delete-account-btn" :hx-post "/delete-account" :disabled true} "Delete Account"]]))

(defn support-component
  []
  (h/html
   [:div {:id "faq-section"}
    [:h2 "Frequently Asked Questions"]
    [:ul
     [:li
      [:p "What features does this app have?"]
      [:p "In this amazing webapp, you can add, edit, and delete your financial transactions."]]
     [:li
      [:p "What type of financial transactions are supported?"]
      [:p "Only invoices and expenses are supported."]]
     [:li
      [:p "How do I reset my password?"]
      [:p "Go to the settings tab, and follow the steps at the bottom to update your password."]]
     [:li
      [:p "Will this webapp have more features?"]
      [:p "No, we currently do not plan to add additional features."]]
     [:li
      [:p "Can I export my financial data?"]
      [:p "No, the webapp doesn't have any export functionality."]]
     [:li
      [:p "Can I use the webapp on mobile devices?"]
      [:p "No, this webapp has been designed for 1920x1080 desktop viewport."]]
     [:li
      [:p "What should I do if I encounter an error?"]
      [:p "If you experience any issues, raise an issue on github: "]
      [:a {:href "https://github.com/kevin-rn/Clojure-FinAdmin"} "https://github.com/kevin-rn/Clojure-FinAdmin"]]
     [:li
      [:p "Do I need an account to use the webapp?"]
      [:p "Yes, otherwise you wouldn't be able to access the dashboard and see this page."]]]]


   [:div
    [:h2 "Feedback"]
    [:p "We value your feedback. Please let us know how we can improve."]
    [:textarea {:placeholder "Your feedback..." :name "description" :class "w-full h-48 resize-none"}]
    [:button {:type "submit" :disabled true} "Submit Feedback"]
    [:div {:id "warning-sign"} [:p  "*This feature has not been implemented."]]]))

(defn dashboard
  [email]
  (str "<!DOCTYPE html>"
       (h/html
        [:html
         [:head
          [:title "Clojure FinAdmin"]
          (include-css "/css/output.css")
          (include-css "/css/dashboard.css")
          (include-js "https://unpkg.com/htmx.org@2.0.4")
          (include-js "https://code.jquery.com/jquery-3.6.0.min.js")
          [:script {:src "/js/app.js" :defer true}]
          [:link {:href "https://fonts.googleapis.com/css?family=Montserrat:400,900" :rel "stylesheet"}]
          [:link {:rel "shortcut icon" :href "/favicon.ico" :type "image/x-icon"}]]
         [:body
          [:header
           [:img {:src "/logo/logo.png" :alt "Logo" :class "logo"}]
           [:p "Financial Administration"]
           [:p email]]

          [:div {:class "flex"}
           [:nav {:id "sidebar" :class "flex"}
            [:ul
             [:li
              [:button {:id "toggle-btn" :onclick "toggleSidebar()"}
               [:img {:src "/icons/toggle.svg" :alt "Toggle button"}]]]
             [:li {:class "selected"} [:a {:href "" :hx-get "/overview" :hx-target "#dashboard-content"}
                                       [:img {:src "/icons/dashboard.svg" :alt "Dashboard Icon"}]
                                       [:span "Overview"]]]
             [:li [:a {:href "" :hx-get "/forms" :hx-target "#dashboard-content"}
                   [:img {:src "/icons/forms.svg" :alt "Forms Icon"}]
                   [:span "Forms"]]]
             [:li
              [:button {:class "dropdown-btn" :onclick "toggleSubMenu(this)"}
               [:img {:src "/icons/bookkeeping.svg" :alt "Bookkeeping"}]
               [:span "Bookkeeping"]
               [:img {:src "/icons/dropdown.svg" :alt "Dropdown Menu"}]]
              [:ul {:class "sub-menu"}
               [:div
                [:li [:a {:href "" :hx-get "/transactions" :hx-target "#dashboard-content"}
                      [:img {:src "/icons/balance.svg" :alt "Transactions Icon"}]
                      [:span "Transaction History"]]]
                [:li [:a {:href "" :hx-get "/expenses" :hx-target "#dashboard-content"}
                      [:img {:src "/icons/expenses.svg" :alt "Expenses Icon"}]
                      [:span "Expenses"]]]
                [:li [:a {:href "" :hx-get "/invoices" :hx-target "#dashboard-content"}
                      [:img {:src "/icons/invoices.svg" :alt "Invoices Icon"}]
                      [:span "Invoices"]]]]]]
             [:li [:a {:href "" :hx-get "/settings" :hx-target "#dashboard-content"}
                   [:img {:src "/icons/settings.svg" :alt "Settings Icon"}]
                   [:span "Settings"]]]
             [:li [:a {:href "" :hx-get "/support" :hx-target "#dashboard-content"}
                   [:img {:src "/icons/support.svg" :alt "Support Icon"}]
                   [:span "Support"]]]
             [:li [:a {:href "/" :hx-get "/logout" :hx-target "this"}
                   [:img {:src "/icons/logout.svg" :alt "Log out Icon"}]
                   [:span "Log out"]]]]]

           [:main {:class "flex items-center justify-center"}
            [:div {:id "dashboard-content" :class "min-w-3/4"}
             (overview-component)]]]]])))
