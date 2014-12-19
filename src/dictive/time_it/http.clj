(ns dictive.time-it.http
  (:require [clojure.set :refer [join]]
            [clojure.string :refer [upper-case]]
            [ring.util.response :refer [resource-response response header status]]))

(defn accepted
  []
  (-> (response nil)
      (status 202)))

(defn bad-request
  []
  (-> (response nil)
      (status 400)))

(defn not-authorized
  []
  (-> (response nil)
      (status 401)))

(defn options
  ([]
   (options #{:options} nil))
  ([allowed]
   (options allowed nil))
  ([allowed body]
   (-> (response body)
       (header "Allow" (join ", " (map (comp upper-case name) allowed))))))

(defn method-not-allowed
  [allowed]
  (-> (options allowed)
      (status 405)))
