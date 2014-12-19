(ns dictive.time-it.tokens
  (:require [clj-jwt.core :as jwt]
            [clj-jwt.key :refer [private-key]]
            [clj-time.core :refer [days now plus]]
            [dictive.time-it.users :as users]))

#_(def rsa-prv-key (private-key "rsa/private.key" "pass phrase"))
#_(def ec-prv-key  (private-key "ec/private.key"))

(defn create
  [email password]
  (if-let [user (users/retrieve email password)]
    (let [claim {:iss (:email user)
                 :exp (plus (now) (days 1))
                 :iat (now)}]
      {:token (-> claim
                  jwt/jwt
                  (jwt/sign :HS512 "secret")
                  jwt/to-str)})))

(defn extract-user
  [str-token]
  (let [jwt-token (-> str-token
                      jwt/str->jwt)]
    (if (jwt/verify jwt-token "secret")
      (-> jwt-token
          :claims
          :iss))))
