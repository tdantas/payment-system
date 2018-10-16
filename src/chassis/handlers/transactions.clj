(ns chassis.handlers.transactions
  (:require [compojure.api.sweet :refer [context GET POST resource]]
            [ring.util.http-response :refer :all]
            [clojure.spec.alpha :as s]
            [spec-tools.spec :as spec]
            [spec-tools.data-spec :as dspec]



            [chassis.http :as http]

            [chassis.services.transaction-refund :as tx-refund]
            [chassis.services.transaction-sale :as tx-sale]

            [metrics.ring.expose :refer [render-metrics serve-metrics]]
            [chassis.repositories.payment-sessions :as ps]
            [chassis.handlers.middlewares :as mw]))

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

(s/def ::tx-refund-request (s/keys :req-un [::amount]))

(defn payment-handler [uuid payment-session tx-payment]
  (http/translate (tx-sale/sale uuid payment-session tx-payment)))

(defn refund-handler [uuid payment-session tx-refund]
  (http/translate (tx-refund/refund uuid payment-session tx-refund)))

(def app
  (context "/payment-sessions/:psid" []
    :tags ["spec"]
    :coercion :spec
    :path-params [psid :- spec/int?]
    :middleware [mw/load-payment-session]

    (POST "/transactions/sale" {{:keys [uuid session]} :app :as r}
      :body [body ::tx-payment-request]
      :middleware [(mw/register-intent "SALE")]
      (payment-handler uuid session body))

    (POST "/transactions/refund" {{:keys [uuid session]} :app}
      :body [body ::tx-refund-request]
      :middleware [(mw/register-intent "REFUND")]
      (refund-handler uuid session body))))