(ns voidwalker.auth
  (:require [compojure.core :refer [defroutes GET POST]]
            [korma.core :as korma]
            [ring.util.http-response :as response]
            [voidwalker.content.core :refer [send-response]]))

(korma/defentity users)


(defn handle-signup [{:keys [username password] :as user}]
  (let [userlist (korma/select users
                               (korma/where {:username username}))]
    (when (empty? userlist)
      (korma/insert users (korma/values user)))))


(defroutes auth-routes
  (GET "/check" []
       (send-response (response/ok "Ok done")))
  (POST "/login" []
        (send-response (response/ok "Not implemented")))
  (POST "/signup" {{:keys [username password] :as user} :params}
        (if-not (nil? (handle-signup user))
          (send-response (response/ok {:msg "Signed up"}))
          (send-response (response/ok {:msg "Wrong"})))))











