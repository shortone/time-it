(ns dictive.time-it.core
  (:require [compojure.core :refer [defroutes context ANY GET POST OPTIONS]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [dictive.time-it.http :as http]
            [dictive.time-it.tokens :as tokens]
            [dictive.time-it.users :as users]
            [ring.middleware.json :as middleware]
            [ring.util.response :refer [resource-response response header status]]))

(defroutes users-routes
  (OPTIONS "/" [] (http/options #{:options :post}))
  (POST "/" {body :body} (if (users/create body)
                           (http/accepted)
                           (http/bad-request)))
  (ANY "/" [] (http/method-not-allowed #{:options :post}))
  (context "/:id" [id]
           (OPTIONS "/" [] (http/options #{:get :options}))
           (GET "/" [] (if-let [user (users/retrieve id)]
                         (response (select-keys user [:_id :email :creation-date :type]))
                         (-> (response nil)
                             (status 404))))
           (ANY "/" [] (http/method-not-allowed #{:get :options}))))

(defroutes tokens-routes
  (OPTIONS "/" [] (http/options #{:options :post}))
  (POST "/" {body :body} (if-let [token (tokens/create (:email body)
                                                       (:password body))]
                           (response token)
                           (http/not-authorized)))
  (ANY "/" [] (http/method-not-allowed #{:options :post}))
  (context "/me" []
           (OPTIONS "/" [] (http/options #{:get :options}))
           (GET "/" {user :user} (response {:email user}))
           (ANY "/" [] (http/method-not-allowed #{:get :options}))))

(defroutes app-routes
  (context "/api" []
           (OPTIONS "/" [] (http/options #{:options} {:version "0.0.1-SNAPSHOT"}))
           (ANY "/" [] (http/method-not-allowed #{:options}))
           (context "/users" [] users-routes)
           (context "/tokens" [] tokens-routes))
  (route/resources "/")
  (route/not-found "Page not found"))

(defn wrap-dir-index
  [handler]
  (fn [req] (handler (update-in req [:uri] #(if (= "/" %) "/index.html" %)))))

(defn wrap-jwt
  [handler]
  (fn
    [req]
    (if-let [authorization (get-in req [:headers "authorization"])]
      (if-let [[[_ token]] (re-seq #"^Bearer\s+(.*)$" authorization)]
        (handler (assoc req :user (tokens/extract-user token)))
        (handler req))
      (handler req))))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body {:keywords? true})
      (middleware/wrap-json-response)
      (wrap-dir-index)
      (wrap-jwt)))
