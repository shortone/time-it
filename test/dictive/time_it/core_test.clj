(ns dictive.time-it.core-test
  (:require [clojure.test :refer :all]
            [dictive.time-it.core :refer :all]
            [ring.mock.request :as mock]))

(deftest test-api-routes
  (testing "API OPTIONS"
    (let [response (app-routes (mock/request :options "/api"))]
      (is (= (:status response) 200))
      (is (contains? (:body response) :version))))
  (testing "API GET"
    (let [response (app-routes (mock/request :get "/api"))]
      (is (= (:status response) 405))
      (is (nil? (:body response)))))
  (testing "Not found"
    (let [response (app-routes (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))

(deftest test-users-routes
  (testing "Users OPTIONS"
    (let [response (app-routes (mock/request :options "/api/users"))]
      (is (= (:status response) 200))
      (is (= (:headers response) {"Allow" "OPTIONS POST"}))))
  (testing "Users POST"
    (let [response (app-routes (mock/request :post "/api/users" {:username "test@dictive.ch"
                                                                 :password "secret"}))]
      (is (= (:status response) 202))
      (is (nil? (:body response)))))
  (testing "Users GET"
    (let [response (app-routes (mock/request :get "/api/users"))]
      (is (= (:status response) 405))
      (is (nil? (:body response))))))
