(ns dictive.time-it.core
  (:require [clojure.string :refer [blank?]]
            [clj-jwt.core :as jwt]
            [clj-jwt.key :refer [private-key]]
            [clj-time.core :refer [days now plus]]
            [com.ashafa.clutch :as clutch]
            [compojure.core :refer [defroutes context ANY GET POST OPTIONS]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [crypto.password.bcrypt :as bcrypt]
            [ring.middleware.json :as middleware]
            [ring.util.response :refer [resource-response response header status]]))

(def db-users (clutch/get-database "time-it/users"))

#_(def rsa-prv-key (private-key "rsa/private.key" "pass phrase"))
#_(def ec-prv-key  (private-key "ec/private.key"))

(defn get-user
  [id & {:keys [by] :or {by :id}}]
  (case by
    :id (clutch/get-document db-users id)
    :email (-> (clutch/get-view db-users "queries" "by_email" {:key id})
               (first)
               (:value))
    nil))

(defn create-new-user
  [{:keys [username password]}]
  (if (and (not (blank? username)) (not (blank? password)))
    (let [hashed-password (bcrypt/encrypt password)
          user {:email username
                :hashed-password hashed-password
                :creation-date (java.util.Date.)
                :type :local}]
      (clutch/put-document db-users user))))

(defn get-token
  [email password]
  (if-let [user (get-user email :by :email)]
    (if (bcrypt/check password (:hashed-password user))
      (let [token {:iss (:email user)
                   :exp (plus (now) (days 1))
                   :iat (now)}]
        {:token (-> token jwt/jwt (jwt/sign :HS512 "secret") jwt/to-str)
         :key (:_id user)}))))

(defroutes users-routes
  (OPTIONS "/" [] (-> (response nil)
                      (header "Allow" "OPTIONS POST")))
  (POST "/" {body :body} (do
                           (create-new-user body)
                           (-> (response nil)
                               (status 202))))
  (ANY "/" []
       (-> (response nil)
           (status 405)
           (header "Allow" "OPTIONS POST")))
  (context "/:id" [id]
           (OPTIONS "/" [] (-> (response nil)
                               (header "Allow" "OPTIONS GET")))
           (GET "/" [] (if-let [user (get-user id)]
                         (response (select-keys user [:_id :email :creation-date :type]))
                         (-> (response nil)
                             (status 404))))
           (ANY "/" []
                (-> (response nil)
                    (status 405)
                    (header "Allow" "OPTIONS GET")))))

(defroutes tokens-routes
  (GET "/" {params :query-params} (if-let [token (get-token (get params "email")
                                                            (get params "password"))]
                                    (response token)
                                    (-> (response nil)
                                        (status 401))))
  (ANY "/" []
       (-> (response nil)
           (status 405)
           (header "Allow" "OPTIONS GET"))))

(defroutes app-routes
  (context "/api" []
           (OPTIONS "/" []
                    (-> (response {:version "0.0.1-SNAPSHOT"})
                        (header "Allow" "OPTIONS")))
           (ANY "/" []
                (-> (response nil)
                    (status 405)
                    (header "Allow" "OPTIONS")))
           (context "/users" [] users-routes)
           (context "/tokens" [] tokens-routes))
  (route/resources "/")
  (route/not-found "Page not found"))

(defn wrap-dir-index
  [handler]
  (fn [req] (handler (update-in req [:uri] #(if (= "/" %) "/index.html" %)))))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body {:keywords? true})
      (middleware/wrap-json-response)
      (wrap-dir-index)))
