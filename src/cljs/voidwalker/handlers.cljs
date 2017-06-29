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

(reg-event-db
  :set-active-page
  (fn [db [_ page]]
    (assoc db :page page)))

(reg-event-db
  :set-docs
  (fn [db [_ docs]]
    (assoc db :docs docs)))

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
