(ns chassis.handlers.spec-test
  (:require [clojure.test :refer :all]
            [cheshire.core :as cheshire]
            [chassis.handler :refer :all]
            [ring.mock.request :as mock]
            [mount.core :as mount]))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(defn  create-request [url & {:keys [method auth accept] 
                              :or {method :get auth "Token 11111111111111" accept "application/json"}}]
  (-> (mock/request method url)
      (mock/header "accept" accept)
      (mock/header "authorization" auth)))

;; make sure to start config, because auth backend depends on it
(defn config-fixture [f]
  (mount/start #'chassis.config/config)
  (f)
  (mount/stop))

(use-fixtures :once config-fixture)

(deftest spec_plus
  (testing "should 200 OK when submitting the proper token"
    (let [req  (create-request "/spec/plus?x=1&y=2")                 
          res  (app req)
          body (parse-body (:body res))]
      (is (= 200 (:status res)))
      (is (= {:total 3} body))
      (is (clojure.string/starts-with? (get-in res [:headers "Content-Type"]) "application/json"))))
  
  (testing "should fail with 400 when arguments don't conform"
    (let [req (create-request "/spec/plus?x=1&y=xxx")
          res (app req)]
      (is (= 400 (:status res)))))

  (testing "should fail with 403 when token is invalid"
    (let [req (create-request "/spec/plus?x=1&y=2" :auth "invalid")
          res (app req)]
      (is (= 403 (:status res))))))
