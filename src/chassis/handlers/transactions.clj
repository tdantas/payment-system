(ns chassis.handlers.transactions
  (:require [compojure.api.sweet :refer [context GET POST resource]]
            [ring.util.http-response :refer :all]
            [clojure.spec.alpha :as s]
            [spec-tools.spec :as spec]
            [spec-tools.data-spec :as dspec]


            [chassis.http :as http]

            [chassis.services.intent :as intent]
            [metrics.ring.expose :refer [render-metrics serve-metrics]]
            [chassis.repositories.payment-sessions :as ps]))

(s/def ::amount spec/number?)
(s/def ::cost spec/number?)
(s/def ::billable spec/number?)

(s/def ::name spec/string?)
(s/def ::qty spec/int?)
(s/def ::total spec/number?)
(s/def ::unit spec/number?)
(s/def ::payment-entity spec/string?)
(s/def ::payment-method-authorization spec/string?)


(s/def ::payment-method #{"cc" "bank"})

(s/def ::item   (s/keys :req-un [::name ::qty ::total ::unit]))
(s/def ::items (s/coll-of ::item))

(s/def ::tx-payment-request (s/keys :req-un [::amount ::items ::payment-method ::payment-entity]
                                    :opt-un [::cost ::billable ::payment-method-authorization]))

(defn payment-handler [payment-session tx-payment]
  (http/translate (intent/sale payment-session tx-payment)))

(defn load-payment-session [handler]
  (fn [{:keys [params] :as request}]
    (if-let [ps (ps/find-by-id (:psid params))]
      (handler (assoc request :ps ps))
      (not-found {:code "NOT_FOUND"}))))

(def app
  (context "/payment-sessions/:psid" []
    :tags ["spec"]
    :coercion :spec
    :path-params [psid :- spec/int?]
    :middleware [load-payment-session]

    (POST "/transactions/sale" request
      :body [body ::tx-payment-request]
      (payment-handler (:ps request) body))))
