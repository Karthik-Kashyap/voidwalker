(ns voidwalker.content.core
  (:require [clojure.string :as str]
            [compojure.core :refer [context defroutes GET POST]]
            [korma.core :as k]
            [ring.util.http-response :as response]))

(korma.db/defdb db (korma.db/mysql {:user "root"
                                    :password ""
                                    :db "voidwalker"}))

(defn- schema-stmts []
  (-> "create-schema.sql"
      clojure.java.io/resource
      slurp
      (str/split #"--;;")))

(defn create-schema []
  (println "Creating schema")
  (doseq [stmt (schema-stmts)]
    (k/exec-raw stmt)))

(k/defentity posts)

;; todo define specs asap
;; this is going to get very confusing

;; (k/insert posts (k/values {:url "/murlocs"
;;                            :content "Summon a 1/1 murloc"}))

(defn add-post [post]
  (k/insert posts (k/values post)))

(defn get-post [url]
  (first (k/select posts
                   (k/where {:url url}))))

(defn send-response [response]
  (-> response
      (response/header "Content-Type" "application/json; charset=utf-8")))

(defroutes content-routes
  (context "/article" []
           (GET "/" {{:keys [url]}  :params}
                (send-response (response/ok (get-post url))))
           (POST "/" {post :params}
                 (add-post post)
                 (send-response (response/ok {:msg "Post added"})))))
