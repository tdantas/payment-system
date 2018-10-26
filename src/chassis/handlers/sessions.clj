(ns chassis.handlers.sessions
  (:require [compojure.api.sweet :refer [routes context GET POST resource]]
            [ring.util.http-response :refer [precondition-failed ok not-found]]
            [clojure.spec.alpha :as s]
            [spec-tools.spec :as spec]
            [spec-tools.core :as st]

            [chassis.braintree :as bt]
            [metrics.ring.expose :refer [render-metrics serve-metrics]]
            [chassis.services.payment-sessions :as ps]
            [cats.monad.exception :as m-exception]
            [cats.monad.identity :as m-identity]
            [cats.core :as m-core]
            [chassis.http :as http]
            [chassis.handlers.middlewares :as mw]))


(s/def ::expiration-date spec/inst?)
(s/def ::email spec/string?)
(s/def ::currency #{"EUR"})

(s/def ::client (s/keys :req-un [::email]))

(s/def ::correlation spec/string?)
(s/def ::session-request (s/keys
                           :req-un [::currency]
                           :opt-un [::correlation ::expiration-date ::client]))
(def app
    (routes
      (POST "/sessions" []
        :body [body ::session-request]
        :middleware [(mw/register-intent "SESSION")]
        (http/translate (ps/create body)))


      (GET "/sessions/:psid" request
        :middleware [mw/load-payment-session]
        :path-params [psid :- spec/int?]
        (ok (get-in request [:app :session])))))


