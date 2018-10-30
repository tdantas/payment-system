(ns chassis.handlers.orders
  (:require [compojure.api.sweet :refer [context GET POST resource]]
            [ring.util.http-response :refer :all]
            [clojure.spec.alpha :as s]
            [spec-tools.spec :as spec]

            [chassis.domains.orders.core :as orders]

            [chassis.http :as http]

            [chassis.services.refund :as tx-refund]
            [chassis.services.sale :as tx-sale]

            [cats.core :refer [bind mlet]]
            [cats.monad.either :refer [either left right]]

            [metrics.ring.expose :refer [render-metrics serve-metrics]]
            [chassis.repositories.payment-sessions :as ps]
            [chassis.handlers.middlewares :as mw]
            [clojure.tools.logging :as log]))

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

(s/def ::refund-order (s/keys :req-un [::amount ::payment-entity]))

(defn mark-order-as-failed [order failure]
  (log/info "marking order [" (:id order) "] as failed")
  (orders/mark-as-failed order)
  (left failure))

(defn mark-order-fullfiled [order _]
  (log/info "marking order [" (:id order) "] as fullfiled")
  (orders/mark-as-fullfiled order))

(defn intent [type web-params f]
  (let [order (orders/build (assoc web-params :type type))]
    (mlet [order (orders/valid? order)
           order (orders/save order)]

     (either (f order)
             (partial mark-order-as-failed order)
             (partial mark-order-fullfiled order)))))

(defn sale-handler [session web-params]
  (-> (intent :sale web-params (partial tx-sale/sale session))
      (http/translate)))

(defn refund-handler [session web-params]
   (-> (intent :refund web-params (partial tx-refund/refund session))
       (http/translate)))

(def app
  (context "/sessions/:psid" []
    :tags ["spec"]
    :coercion :spec
    :path-params [psid :- spec/int?]
    :middleware [mw/load-payment-session]

    (POST "/sale" {{:keys [uuid session]} :app :as r}
      :body [body ::sale-order]
      :middleware [(mw/register-intent "PAYMENT")]
      (sale-handler session (assoc body :uuid uuid :session-id (:id session))))

    (POST "/refund" {{:keys [uuid session]} :app}
      :body [body ::refund-order]
      :middleware [(mw/register-intent "REFUND")]
      (refund-handler session (assoc body :uuid uuid :session-id (:id session))))))