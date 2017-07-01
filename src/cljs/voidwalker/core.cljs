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
            [clojure.core.async :as async]
            [voidwalker.subscriptions]
            [re-frisk-remote.core :refer [enable-re-frisk-remote!]])
  (:import goog.History))

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:img {:src (str js/context "/img/warning_clojure.png")}]]]])


(defn get-value [e]
  (-> e .-target .-value))


(defn navbar []
  [:nav.navbar.navbar-default>div.container-fluid
   [:div.navbar-header>a.navbar-brand {:href "/"} "Entranceplus"]
   [:div.nav.navbar-nav
    [:li {:class (when (= @(rf/subscribe [:page]) :add)
                   "active")}
     [:a {:href "/add"} "Add"]]]])


(defn input [{:keys [state placeholder type]}]
  [:div.form-group [:input.form-control
                    {:placeholder placeholder
                     :value @state
                     :type (or type "text")
                     :on-change #(reset! state (-> % get-value))}]])

;;;;;;;;;;;;;;;;;;;;;;
;; new article form ;;
;;;;;;;;;;;;;;;;;;;;;;

(defn progress-info [data]
  (let [{:keys [class value]} (case data
                                :loading {:class "alert-info"
                                          :value "Saving Boss.."}
                                :success {:class "alert-success"
                                          :value "Saved.."}
                                :error {:class "alert-fail"
                                        :value "Failed to save"}
                                {:class "hidden"
                                 :value "Should not be seen.."})]
    [:div>div.alert {:class class
                     :role "alert"} value]))


(defn add-post []
  (let [url (r/atom "")
        tags (r/atom "")
        content (r/atom "")
        title (r/atom "")
        post-status (rf/subscribe [:new/post-status])]
    (fn []
      ;;todo clean this up
      (when (= @post-status :success)
        (do (reset! url "")
            (reset! tags "")
            (reset! content "")
            (reset! title "")))
      [:div.container
       [:h1 "New Article"]
       [:form
        [input {:state url
                :placeholder "Enter url"}]
        [input {:placeholder "Comma separated keywords/tags"
                :state tags}]
        [input {:placeholder "Enter title"
                :state title}]
        [:div.form-group [q/editor
                          {:id "my-quill-editor-component-id"
                           :content ""
                           :selection nil
                           :on-change-fn (fn [source data]
                                           (when (= source "user")
                                             (reset! content data)))}]]
        [:div.form-group>button.btn.btn-primary
         {:on-click (fn [e]
                      (.preventDefault e)
                      (rf/dispatch [:save-article {:url @url
                                                   :tags @tags
                                                   :content @content
                                                   :title @title}]))}
         "Save article"]
        [progress-info @post-status]]])))


;;;;;;;;;;;;;;;
;; home-page ;;
;;;;;;;;;;;;;;;

(defn home-page []
  [:div.container
   [:h1 "List of Posts"]
   (map (fn [article]
          [:div (:title article)])
        @(rf/subscribe [:articles]))])


(def pages
  {:home #'home-page
   :add #'add-post})


(defn page []
  [:div
   [navbar]
   [(pages @(rf/subscribe [:page]))]])

;;;;;;;;;;;;
;; routes ;;
;;;;;;;;;;;;

(secretary/set-config! :prefix "")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :home]))

(secretary/defroute "/add" []
  (rf/dispatch [:set-active-page :add]))

(secretary/defroute "/about" []
  (rf/dispatch [:set-active-page :about]))

(accountant/configure-navigation! {:nav-handler  (fn [path]
                                                   (println "path" path)
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

(.addEventListener js/document
                   "DOMContentLoaded"
                   #(secretary/dispatch! (.. js/document -location -pathname)))

;; -------------------------
;; Initialize app

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (load-interceptors!)
  (enable-re-frisk-remote!)
  (hook-browser-navigation!)
  (mount-components))
