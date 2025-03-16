(ns finadmin.views.dashboardview
  (:require
   [clojure.data.json :as json]
   [finadmin.views.helpers :as helpers]
   [hiccup.page :refer [include-css include-js]]
   [hiccup2.core :as h]))


(defn overview-component
  "Generates the overview section of the dashboard, including:
     - Total expenses and invoices, broken down by currency.
     - Pie chart displaying the distribution of expenses by type.
     - Stacked bar chart showing the currency distribution of expenses and invoices.
     - Recent transactions for both expenses and invoices.
  
     Parameters:
     - `{:keys [expdata invdata]}`: A map containing expense data (`expdata`) and invoice data (`invdata`).
     
     Returns:
     - An HTML structure for the overview section of the dashboard."
  [{:keys [expdata invdata]}]
  (let [totalExpense (helpers/total-amount-per-currency expdata)
        totalInvoice (helpers/total-amount-per-currency invdata)
        expenseType (helpers/count-by-key :expenses/expense_type expdata)
        chartData   (json/write-str (helpers/aggregate-transactions 
                                     (helpers/group-by-month-and-currency 
                                      (concat expdata invdata))))
        dataStr (str "var expenseTypeData = " expenseType "; "
                     "var chartData = " chartData "; ")
        jscode (str dataStr "createExpensePieChart(); createStackedBarChart(); ")]
    (h/html
     [:div {:id "overview-component" :class "grid grid-cols-2 grid-rows-4"}
      [:div {:class "chart-div col-span-1 row-span-1"}
       [:h2 "Overview"]
       [:div {:class "grid grid-cols-2"}
        [:div
         [:h5 "Invoices - Total Amount"]
         (for [[currency amount] totalInvoice]
           [:p (str (name currency) ": " amount)])]
        [:div 
         [:h5 "Expenses  - Total Amount"]
         (for [[currency amount] totalExpense]
           [:p (str (name currency) ": " amount)])]]]
      [:div {:class "chart-div col-span-1 row-span-3"}
       [:h3 "Expense Types"]
       [:canvas#expensePieChart]]
      [:div {:class "chart-div col-span-1 row-span-2"}
       [:h3 "Currency distribution - Invoices & Expenses"]
       [:canvas#stackedBarChart]]
      [:div {:class "chart-div col-span-2 row-span-1"}
       [:h3 "Recent Transactions"]
       [:div {:class "grid grid-cols-2"}
        [:h5 "Expenses"]
        [:h5 "Invoices"]
        [:table {:class "w-full"}
         [:thead
          [:tr
           [:th {:class "font-semibold text-left"} "Date"]
           [:th {:class "font-semibold text-left"} "Amount"]
           [:th {:class "font-semibold text-left"} "Currency"]
           [:th {:class "font-semibold text-left"} "Payment method"]]]
         (if (empty? expdata)
           [:tbody
            [:tr {:class "text-center non-items"}
             [:td {:colspan "6"} [:i.select-none "No Transactions stored"]]]]
           [:tbody
            (for [{:transactions/keys [transaction_date amount currency payment_method]} (take 3 expdata)]
              [:tr {:class "border-b non-items"}
               [:td (helpers/parse-and-format-date transaction_date)]
               [:td amount]
               [:td currency]
               [:td payment_method]])])]

        [:table {:class "w-full"}
         [:thead
          [:tr
           [:th {:class "font-semibold text-left"} "Date"]
           [:th {:class "font-semibold text-left"} "Amount"]
           [:th {:class "font-semibold text-left"} "Currency"]
           [:th {:class "font-semibold text-left"} "Payment method"]]]
         (if (empty? invdata)
           [:tbody
            [:tr {:class "text-center non-items"}
             [:td {:colspan "6"} [:i.select-none "No Transactions stored"]]]]
           [:tbody
            (for [{:transactions/keys [transaction_date amount currency payment_method]} (take 3 invdata)]
              [:tr {:class "border-b non-items"}
               [:td (helpers/parse-and-format-date transaction_date)]
               [:td amount]
               [:td currency]
               [:td payment_method]])])]]]
      
      ;; NOTE: Converted Clojure data to Json string and declared var in Javascript to be used
      ;; before calling its methods. Probably better not to parse the entire data to json but chunk it instead.
      [:script {:type "text/javascript" :defer true} (h/raw jscode)]])))


(defn forms-component
  "Generates the form for uploading documents related to financial transactions. This includes:
     - A file upload dropzone allowing multiple document uploads.
     - Fields for entering transaction date, associated entities (e.g., contractors, suppliers), and document type (e.g., Invoice).
     - A warning message indicating the document upload feature is not yet implemented.
  
     Returns:
     - An HTML structure for the document upload form."
  []
  (h/html
   [:div#forms-component
    [:h2 "Upload Forms or Documents"]
    [:form {:class "grid grid-cols-3"
            :hx-post "/add-document"
            :hx-target "#forms-component"} 
     [:fieldset {:class "col-span-3 border p-4 rounded"}
      [:legend "Document Details"]
      [:div {:class "flex items-center justify-center w-full"}
       [:label
        {:for "dropzone-file",
         :class "flex flex-col items-center justify-center w-3/4 dropzone"}
        [:div
         {:class "flex flex-col items-center justify-center"}
         [:img {:src "/icons/upload.svg"}]
         [:p [:span "Click to upload"] " or drag and drop"]
         [:p "Allowed formats: " [:i "PDF, Excel (XLSX, CSV), DOCX, Images"]]]
        [:input {:id "dropzone-file"
                 :type "file"
                 :accept ".pdf,.xls,.xlsx,.csv,.docx,.jpg,.png,.gif,.svg"
                 :class "hidden"
                 :onchange "getFileData(event)"
                 :multiple true}]]]
      [:script (h/raw "window.onload = dragAndDrop();")]
      [:div {:class "flex flex-col items-center justify-center w-full"} 
       [:ul#document-files
        [:li [:p "This is an example document - upload file(s) above to see other filenames.pdf"]
         [:button {:class "remove-file" :onclick "this.parentElement.remove();"} "✖" ]]]]
      [:div {:class "grid grid-cols-3"}
       [:div.input-group
        [:label "Transaction Date:"]
        [:input {:type "date"
                 :name "transaction_date"
                 :required true}]]
       [:div.input-group
        [:label "Associated Entities:"]
        [:input {:type "text"
                 :name "associated_entities"
                 :placeholder "Contractor, Supplier, etc."
                 :required true}]]
       [:div.input-group
        [:label "Document Type:"]
        [:div.custom-select
         [:select {:name "document_type"
                   :required true}
          (for [type helpers/document-types]
            [:option {:value type
                      :selected (if (= type "Invoice") "selected" nil)} type])]
         [:span.custom-select-arrow]]]
       [:div {:class "input-group col-span-3"}
        [:label "Description:"]
        [:textarea {:name "description"
                    :placeholder "Write a detailed description here..."
                    :class "w-full resize-none"}]]]]

     [:button {:type "submit" :disabled true} "Upload document"]
     [:div {:id "warning-sign" :class "flex justify-end"} [:p  "*This feature has not been implemented."]]]]

   [:div
    [:h2 "Documents:"]
    [:table {:class "w-full" :id "transaction-table"}
     [:thead
      [:tr
       [:th {:class "font-semibold text-left" :onclick "sortTable(0, this)"} "Date"
        [:span.sort-icon
         [:img {:src "/icons/dropdown.svg" :id "sort-btn-initial" :alt "Sort asc"}]]]
       [:th {:class "font-semibold text-left" :onclick "sortTable(1, this)"} "Document Name" [:span.sort-icon]]
       [:th {:class "font-semibold text-left" :onclick "sortTable(2, this)"} "Transaction Date" [:span.sort-icon]]
       [:th {:class "font-semibold text-left" :onclick "sortTable(3, this)"} "Associated Entities" [:span.sort-icon ""]]
       [:th {:class "font-semibold text-left" :onclick "sortTable(4, this)"} "Document Type" [:span.sort-icon]]
       [:th {:class "font-semibold text-left" :onclick "sortTable(5, this)"} "Description" [:span.sort-icon]]]]
     [:tbody
      [:tr {:class "text-center non-items"}
       [:td {:colspan "6"} [:i.select-none "No Documents stored"]]]]
     ]]))


(defn settings-component
  "Generates the settings page for the user, allowing them to view and update their profile information, including:
     - Displaying user email, password, and creation date.
     - A form for updating the user's password, including error handling.
     - A section to warn the user about the permanent deletion of their account.
     
     Parameters:
     - `account`: A map representing the user's account details, including email, password, and account creation date.
     - `{:keys [error modal]}`: A map containing potential error messages and modal-related popup for the settings page.
     
     Returns:
     - An HTML structure for the settings page, including user profile details, password reset form, and account deletion warning."
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
      [:p [:i (helpers/parse-and-format-date (:accounts/created_at account))]]]]]

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
   (helpers/modal-component modal)

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
   "Generates the support section of the dashboard, which includes:
     - A FAQ section with common questions and answers about the app.
     - A feedback form for users to submit their thoughts or suggestions.
     - A placeholder indicating that the feedback feature is not yet implemented.
     
     Returns:
     - An HTML structure for the support page, including FAQs and the feedback form."
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
  "Generates the entire dashboard page, including:
     - The page structure with header, sidebar, and main content area.
     - A navigation menu for accessing different sections of the dashboard.
     - A dynamically loaded content section based on user interaction (hx-get for partial updates).
     
     Parameters:
     - `email`: The email of the logged-in user, to be displayed in the header.
     - `data`: A map containing the user's transaction data for rendering the overview component.
     
     Returns:
     - A complete HTML structure for the dashboard page."
  [email data]
  (str "<!DOCTYPE html>"
       (h/html
        [:html
         [:head
          [:title "Clojure FinAdmin"]
          (include-css "/css/output.css")
          (include-css "/css/dashboard.css")
          (include-js "https://unpkg.com/htmx.org@2.0.4")
          (include-js "https://code.jquery.com/jquery-3.6.0.min.js")
          (include-js "https://cdn.jsdelivr.net/npm/chart.js")
          (include-js "https://d3js.org/d3-color.v1.min.js")
          (include-js "https://d3js.org/d3-interpolate.v1.min.js")
          (include-js "https://d3js.org/d3-scale-chromatic.v3.min.js")
          (include-js  "/js/plot.js")
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
             (overview-component data)]]]]])))
