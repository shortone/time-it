(ns dictive.time-it.core
  (:require [clojure.string :refer [blank?]]
            [com.ashafa.clutch :as clutch]
            [compojure.core :refer [defroutes context ANY GET POST OPTIONS]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [crypto.password.bcrypt :as bcrypt]
            [ring.middleware.json :as middleware]
            [ring.util.response :refer [resource-response response header status]]))

(def db-users (clutch/get-database "time-it/users"))

(defn create-new-user
  [{:keys [username password]}]
  (if (and (not (blank? username)) (not (blank? password)))
    (let [hashed-password (bcrypt/encrypt password)
          user {:email username
                :hashed-password hashed-password
                :creation-date (java.util.Date.)
                :type :local}]
      (clutch/put-document db-users user))))

(defn get-user
  [id]
  (if-let [user (clutch/get-document db-users id)]
    (select-keys user [:email :creation-date :type])))

(defn get-user-by-email
  [email]
  (-> (clutch/get-view db-users "queries" "by_email" {:key email})
      (first)
      (:value)))

(defn get-token
  [email password]
  (if-let [user (get-user-by-email email)]
    (if (bcrypt/check password (:hashed-password user))
      user)))

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
                         (response user)
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
