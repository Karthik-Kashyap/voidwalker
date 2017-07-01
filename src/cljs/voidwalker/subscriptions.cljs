(ns voidwalker.subscriptions
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :page
  (fn [db _]
    (:page db)))

(reg-sub
  :docs
  (fn [db _]
    (:docs db)))

(reg-sub
 :new/post-status
 (fn [db _]
   (:new/post-status db)))

(reg-sub
 :articles
 (fn [db _]
   (:articles db)))
