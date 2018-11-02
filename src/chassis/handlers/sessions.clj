(ns chassis.handlers.sessions
  (:require [compojure.api.sweet :refer [routes context GET POST resource]]
            [ring.util.http-response :refer [precondition-failed ok not-found]]
            [clojure.spec.alpha :as s]
            [spec-tools.spec :as spec]

            [metrics.ring.expose :refer [render-metrics serve-metrics]]
            [chassis.http :as http]
            [chassis.services.session :as session]
            [chassis.handlers.middlewares :as mw]))

(s/def ::expiration-date spec/inst?)
(s/def ::email spec/string?)
(s/def ::currency #{"EUR"})

(s/def ::client (s/keys :req-un [::email]))

(s/def ::correlation spec/string?)
(s/def ::session-request (s/keys
                           :req-un [::currency]
                           :opt-un [::correlation ::client]))

(defn session-balance-handler [session]
 (-> (session/balance session)
     (http/translate)))

(def app
    (routes
      (POST "/sessions" []
        :body [body ::session-request]
        :middleware [(mw/request-log "SESSION")]
        (http/translate
          (session/create body)))

      (GET "/sessions/:psid/balance" {{:keys [session]} :app :as r}
        :middleware [mw/load-payment-session]
        :path-params [psid :- spec/int?]
        (session-balance-handler session))))


