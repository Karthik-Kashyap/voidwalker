(ns voidwalker.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [voidwalker.ajax :refer [load-interceptors!]]
            [voidwalker.handlers]
            [accountant.core :as accountant]
            [cljsjs.quill]
            [voidwalker.quill :as q]
            [voidwalker.subscriptions])
  (:import goog.History))

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:img {:src (str js/context "/img/warning_clojure.png")}]]]])

(defn editor []
  [:div [q/editor
         {:id "my-quill-editor-component-id"
          :content "welcome to reagent-quill!"
          :selection nil
          ;; :on-change-fn #(if (= % "user")
          ;;                  (println (str "text changed: " %2)))
          }]])

(defn add-post []
  [:div.container
   [:h1 "New Article"]
   [:form
    [:div.form-group>input.form-control {:placeholder "Enter url"}]
    [:div.form-group>input.form-control {:placeholder "Comma separated keywords/tags"}]
    [editor]]])

(defn home-page []
  [:div [add-post]])

(def pages
  {:home #'home-page
   :about #'about-page})

(defn page []
  [:div
   [(pages @(rf/subscribe [:page]))]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :home]))

(secretary/defroute "/about" []
  (rf/dispatch [:set-active-page :about]))

(accountant/configure-navigation! {:nav-handler  (fn [path]
                                                   (secretary/dispatch! path))
                                   :path-exists? (fn [path]
                                                   (secretary/locate-route path))})
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
(defn fetch-docs! []
  (GET "/docs" {:handler #(rf/dispatch [:set-docs %])}))

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (load-interceptors!)
  (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
