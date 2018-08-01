(ns user-management.core
  (:require [reagent.core :as reagent]
            [reagent.session :as session]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [user-management.ajax :refer [load-interceptors!]]
            [ajax.core :refer [GET POST]]
            [secretary.core :as secretary :include-macros true]
            [antizer.reagent :as ant]
            [goog.crypt.base64 :as b64]
            [clojure.string :as string]
            [cljsjs.moment])
  (:import goog.History))

(def app-state (reagent/atom {}))
(def pages (reagent/atom {:current nil}))

(defn current-page []
  (:current @pages))

(defn content-area []
  [ant/layout-content {:class "content-area" :style {:margin "24px 24px 0px" :height "100%"}}
   [:div 
    [current-page]]])

(defn side-menu [collapsed]
  (fn []
    [ant/menu {:mode "inline" 
               :theme "dark" 
               :inline-collapsed (:menu @app-state)
               :default-selected-keys ["1"]
               :default-open-keys ["sub1"]
               :style (:sider-style @app-state)}
     [ant/menu-item {:disabled true} "Main"]
     [ant/menu-item (reagent/as-element [:span [ant/icon {:type "home" :style {:color "red"}}] 
                                         [:a {:href "/dashboard"} "Dashboard"]])]
     [ant/menu-sub-menu {:title (reagent/as-element [:span [ant/icon {:type "car" :style {:color "red"}}] "Customers"])}
      [ant/menu-item [:a {:href "#"}] "All customers"]
      [ant/menu-item [:a {:href "#"}] "Add new"]
      ]
     
     [ant/menu-sub-menu {:title (reagent/as-element [:span [ant/icon {:type "swap" :style {:color "red"}}] "Products"])}
      [ant/menu-item [:a {:href "#"}] "All products"]
      [ant/menu-item [:a {:href "#"}] "Add new"]
      [ant/menu-item [:a {:href "#"}] "Reports"]]
     
     [ant/menu-sub-menu {:title (reagent/as-element [:span [ant/icon {:type "setting" :style {:color "red"}}] "Settings"])}
      [ant/menu-item [:a {:href "#"}] "User details"]
        [ant/menu-item [:a {:href "#"}] "Test#1"]
      [ant/menu-item [:a {:href "#"}] "Test#2"]]]))

(defn logout-handler [response]
  (.log js/console (str response)))

(defn logout! []
  (POST "/user/logout" 
        {:handler logout-handler
         :error-handler (fn [r] (prn r))}))

(defn page-header []
  [ant/layout-header {:style {:padding "0px"}}
   [:div {:class "header"}
    [:div {:class "right"}
     [:span {:class "action search header_search"}
      [ant/icon {:type "search"}]
      [:div {:class "nothing"}]]
     [:span {:class "action notice_button"}
      [ant/badge {:count 12} [ant/icon {:class "icon" :type "bell"}]]]
     [:span {:class "action account"}
      [ant/avatar {:style {:background-color "#108ee9"} :shape "circle" :class "avatar ant-avatar-sm" :icon "user"}]
      [:span {:class "name"}
       (str 
        (session/get :identity)
        " | " )
       [:a {:href "#"
            :on-click #(logout!)} "logout"]]]]]])

(defn page-footer []
  [ant/layout-footer {:style {:padding "0px"}}
   [:div {:class "global_footer"}
    [:div {:class "links"}
     [:a {:href "#"} "About"]
     [:a {:href "#"} "Jobs"]
     [:a {:href "#"} "Contact"]]
    [:div {:class "copyright"}
     "Copyright "
     [ant/icon {:type "copyright"}]
     "User management demo."]]])

(defn sider-logo []
  [:div {:class "logo"}
   [:a {:href "#"}
    [:h1 "Abdullah"]]])

(def form-style {:label-col {:span 10}
                 :wrapper-col {:span 13}})

(defn encoding [email password]
  (->> [email password]
       (string/join ":")
       (b64/encodeString)
       (str "Basic ")))

(defn handle-login-errors [id]
  (let [{:keys [status body]} (:login-msg @app-state)
        msg (:msg body)]
    (.log js/console msg)
    (if (or 
         (= status 200)
         (= status 201))
      (do
        (ant/message-success msg)
        (session/put! :identity id))
      (ant/message-error msg))))

(defn login-handler [response id]
  (.log js/console (str "response: " response))
  (swap! app-state assoc :login-msg response)
  (handle-login-errors id))

(defn login! [email password]
  (POST "/user/login"
        {:format :json
         :headers {"Authorization" (encoding (string/trim email) password)}
         :handler (fn [response] 
                    (login-handler response email))
         :keywords? true
         :response-format :json
         :error-handler (fn [r] (prn r))}))

(defn get-by-id [element]
  (.-value (.getElementById js/document element)))

(defn login-form [x]
  (fn []
    (let [login-form (ant/get-form)]
      [ant/form {:layout "horizontal"}
       [ant/form-item (merge form-style {:label "Email"})
        (ant/decorate-field login-form "email" {:rules [{:required true}]} [ant/input])]
       [ant/form-item (merge form-style {:label "Password"})
        (ant/decorate-field login-form "password" {:rules [{:required true}]} [ant/input])]
       [ant/form-item {:wrapper-col {:offset 6}}
        [ant/col {:span 4}
         [ant/button {:type "primary" :on-click (fn []
                                                  (do 
                                                    (ant/validate-fields login-form)
                                                    (login! 
                                                     (get-by-id "email")
                                                     (get-by-id "password"))))}
          "Submit"]]
        [ant/col {:offset 1}
         [ant/button {:on-click #(ant/reset-fields login-form)}
          "Reset"]]]])))

(defn login-page []
  (fn []
    [ant/row {:gutter 24}
     [ant/col {:span 12 :offset 6}
      [ant/card {:title "Login" 
                 :style {:position "fixed" 
                         :top "30%" 
                         :left "40%" 
                         :width "50em" 
                         :margin-top "-9em"
                         :margin-left "-15em"}}
       [:p 
        (ant/create-form (login-form false))]]]]))

(defn app []
  (fn []
    [:div {:class (:screen-size @app-state)}
     (if (session/get :identity)
       [ant/locale-provider {:locale (ant/locales "en_US")}
        [ant/layout
         [ant/layout-sider {:style {:background "#001529"}}
          [sider-logo]
          [side-menu]]
         [ant/layout
          [page-header]
          [content-area]
          [page-footer]]]]
       [login-page])]))

;; -------------------------
;; Routes

(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (swap! pages assoc :current [:h1 "Welcome to Home."]))

(secretary/defroute "/about" []
  (swap! pages assoc :current [:h1 "User management demo."]))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
        (events/listen
          HistoryEventType/NAVIGATE
          (fn [event]
            (secretary/dispatch! (.-token event))))
        (.setEnabled true)))

;; -------------------------
;; Initialize app

(defn mount-components []
  (reagent/render [app] (.getElementById js/document "app")))

(defn init! []
  (load-interceptors!)
  (hook-browser-navigation!)
  (session/put! :identity js/user)
  (mount-components))
