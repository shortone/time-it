(ns dictive.time-it.core
  (:require [compojure.core :refer [defroutes context GET]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
            [ring.util.response :refer [resource-response response]]))

(defroutes app-routes
  (context "/api" []
           (context "/status" []
                    (GET "/" [] (response {:version "0.0.1-SNAPSHOT"}))))
  (route/resources "/")
  (route/not-found "Page not found"))

(defn wrap-dir-index
  [handler]
  (fn [req] (handler (update-in req [:uri] #(if (= "/" %) "/index.html" %)))))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)
      (wrap-dir-index)))
