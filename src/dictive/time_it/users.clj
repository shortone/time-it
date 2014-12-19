(ns dictive.time-it.users
  (:require [clojure.string :refer [blank?]]
            [com.ashafa.clutch :as clutch]
            [crypto.password.bcrypt :as bcrypt]))

(def db-users (clutch/get-database "time-it/users"))

(defn- is-valid?
  [stuff]
  (not (blank? stuff)))

(defn retrieve-by-email
  [email]
  (-> (clutch/get-view db-users "queries" "by_email" {:key email})
      (first)
      (:value)))

(defn- push-creation
  [new-user]
  (when (nil? (retrieve-by-email (:email new-user)))
    (clutch/put-document db-users new-user)))

(defn create
  [{:keys [username password]}]
  (if (and (is-valid? username)
           (is-valid? password))
    (do
      (push-creation {:email username
                      :hashed-password (bcrypt/encrypt password)
                      :creation-date (java.util.Date.)})
      true)
    false))


(defn retrieve
  ([id]
   (retrieve id :id))
  ([id by]
   (case by
     :id (clutch/get-document db-users id)
     :email (-> (clutch/get-view db-users "queries" "by_email" {:key id})
                (first)
                (:value))
     nil)))

(defn retrieve
  ([id]
   (clutch/get-document db-users id))
  ([email password]
   (if-let [user (-> (clutch/get-view db-users "queries" "by_email" {:key email})
                     (first)
                     (:value))]
     (if (bcrypt/check password (:hashed-password user))
       user))))
