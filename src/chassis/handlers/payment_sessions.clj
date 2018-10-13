(ns chassis.handlers.payment-sessions
  (:require [compojure.api.sweet :refer [routes context GET POST resource]]
            [ring.util.http-response :refer [precondition-failed ok not-found]]
            [clojure.spec.alpha :as s]
            [spec-tools.spec :as spec]

            [chassis.braintree :as bt]
            [metrics.ring.expose :refer [render-metrics serve-metrics]]
            [chassis.repositories.payment-sessions :as ps]
            [cats.monad.exception :as m-exception]
            [cats.monad.identity :as m-identity]
            [cats.core :as m-core]
            [chassis.http :as http]))


(s/def ::expiration-date spec/inst?)
(s/def ::email spec/string?)
(s/def ::client (s/keys :req-un [::email]))

(s/def ::correlation spec/string?)
(s/def ::session-request (s/keys :opt-un [::correlation ::expiration-date ::client]))

(defn register [body]
  (m-exception/try-on
    (let [customer (bt/customer-create body)]
      (ps/create (merge body {:customer-id (:id customer)})))))

(defn load-payment-session [handler]
  (fn [{:keys [params] :as request}]
    (if-let [ps (ps/find-by-id (:psid params))]
      (handler (assoc request :ps ps))
      (not-found {:code "NOT_FOUND"}))))

(def app
    (routes
      (POST "/payment-sessions" []
        :body [body ::session-request]
        (-> (m-identity/identity body)
            (m-core/bind register)
            http/translate))


      (GET "/payment-sessions/:psid" request
        :middleware [load-payment-session]
        :path-params [psid :- spec/int?]
        (ok (:ps request)))))


