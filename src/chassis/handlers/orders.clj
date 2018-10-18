(ns chassis.handlers.orders
  (:require [compojure.api.sweet :refer [context GET POST resource]]
            [ring.util.http-response :refer :all]
            [clojure.spec.alpha :as s]
            [spec-tools.spec :as spec]
            [spec-tools.data-spec :as dspec]



            [chassis.http :as http]

            [chassis.services.transaction-refund :as tx-refund]
            [chassis.services.sale :as tx-sale]

            [cats.core :refer [bind mlet]]

            [metrics.ring.expose :refer [render-metrics serve-metrics]]
            [chassis.repositories.payment-sessions :as ps]
            [chassis.handlers.middlewares :as mw]
            [chassis.domains.order :as o]))

(s/def ::amount spec/number?)
(s/def ::cost spec/number?)
(s/def ::billable spec/number?)
(s/def ::currency #{"EUR"})

(s/def ::name spec/string?)
(s/def ::qty spec/int?)
(s/def ::total spec/number?)
(s/def ::unit spec/number?)
(s/def ::payment-entity spec/string?)
(s/def ::payment-method-authorization spec/string?)

(s/def ::payment-method #{"cc" "bank"})

(s/def ::item   (s/keys :req-un [::name ::qty ::total ::unit]))
(s/def ::items (s/coll-of ::item))

(s/def ::sale-order (s/keys :req-un [::amount ::items ::payment-method ::payment-entity ::currency]
                            :opt-un [::payment-method-authorization]))

(s/def ::refund-order (s/keys :req-un [::amount]))

(defn payment-handler [uuid session web-params]
  (let [order (o/build session (assoc web-params :uuid uuid))]
    (-> (o/valid? order)
        (bind o/save)
        (bind (partial tx-sale/sale session))
        (http/translate))))

(defn refund-handler [uuid session tx-refund]
  (http/translate (tx-refund/refund uuid session tx-refund)))

(def app
  (context "/sessions/:psid" []
    :tags ["spec"]
    :coercion :spec
    :path-params [psid :- spec/int?]
    :middleware [mw/load-payment-session]

    (POST "/payment" {{:keys [uuid session]} :app :as r}
      :body [body ::sale-order]
      :middleware [(mw/register-intent "PAYMENT")]
      (payment-handler uuid session body))

    (POST "/refund" {{:keys [uuid session]} :app}
      :body [body ::refund-order]
      :middleware [(mw/register-intent "REFUND")]
      (refund-handler uuid session body))))