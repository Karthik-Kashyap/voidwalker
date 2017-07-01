(ns voidwalker.handlers
  (:require [voidwalker.db :as db]
            [ajax.core :as ajax]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx]]
            [day8.re-frame.http-fx]))

(defn new-request [request]
  (merge request {:timeout 8000
                  :response-format (ajax/json-response-format
                                    {:keywords? true})
                  :format (ajax/json-request-format)
                  :on-failure [:error-result]}))

(reg-event-db
 :error-result
 (fn [db _]
   (println "Error occurred")
   (assoc db :error? true)))

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; pagination and initial data loading ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(reg-event-fx
  :set-active-page
  (fn [{:keys [db]} [_ page]]
    (let [rep {:db (assoc db :page page)}]
      (if (= :page :home)
        (merge rep {:dispatch [:get-articles]})
        rep))))

;;;;;;;;;;;;;;;;;;;;;
;; listing article ;;
;;;;;;;;;;;;;;;;;;;;;

(reg-event-fx
 :get-articles
 (fn [_ _]
   {:http-xhrio (new-request {:method :get
                              :uri "/article"
                              :on-success [:set-article]})}))

(reg-event-db
 :set-article
 (fn [db [_ articles]]
   (println articles)
   (assoc db :articles articles)))


;;;;;;;;;;;;;;;;;;;;
;; saving article ;;
;;;;;;;;;;;;;;;;;;;;

(reg-event-fx
 :save-article
 (fn [_ [_ article]]
   {:http-xhrio (new-request {:method :post
                              :uri "/article"
                              :params article
                              :on-success [:article-saved]})}))

(reg-event-db
 :article-saved
 (fn [db _]
   (assoc db :article-saved true)))
