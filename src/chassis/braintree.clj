(ns chassis.braintree
  (:require [chassis.config :refer [config]]
            [mount.core :refer [defstate]]
            [braintree-clj.core :as bt-core]
            [braintree-clj.gateway.core :as g]
            [braintree-clj.gateway.customer :as bt-cg]
            [braintree-clj.customer-request :as bt-cr]
            [braintree-clj.customer :as bt-customer]
            [braintree-clj.result :as result]

            [braintree-clj.core :as braintree]
            [braintree-clj.transaction-request :as bt-tr]
            [braintree-clj.result :as bt-r]
            [braintree-clj.transaction :as bt-tx]

            [clojure.tools.logging :as log]
            [braintree-clj.transaction-request :as bt-tr]
            [cats.monad.either :as m-either]
            [chassis.failure :refer [failure exception]]
            [clojure.stacktrace :as st]))

(defn payment-entity->merchant [payment-entity]
  "runtime")

(defn capture-stack-trace [e]
  (with-out-str
    (st/print-stack-trace e)))

(defn build-gateway [conf]
   (cond
     (nil? (:braintree conf)) (bt-core/create-gateway)
     :else  (bt-core/create-gateway (:braintree conf))))

(defstate gateway
          :start (build-gateway config))

(defn customer-request [{:keys [email first-name last-name :as params]}]
  (bt-cr/create params))

(defn customer-create
  ([params] (customer-create gateway params))
  ([gateway cr-params]
   (let [customer-result (bt-cg/create gateway (customer-request cr-params))
         customer (result/target customer-result)]
        (if (result/success? customer-result)
          (m-either/right (bt-customer/to-map customer))
          (m-either/left (ex-info "cant create customer" {:errors (result/errors customer-result)}))))))


(defn- tx-request [{:keys [payment-method-nonce customer-id amount order-id merchant]}]
  (bt-tr/create {:customer-id           customer-id
                 :amount                amount
                 :merchant-account-id   merchant
                 :submit-for-settlement true
                 :payment-method-nonce  payment-method-nonce
                 :order-id              order-id}))

(defn- bt-tx-sale [{:keys [customer-id amount payment-method-authorization payment-entity correlation]}]
  (let [result (braintree/tx-sale (tx-request {:customer-id          customer-id
                                               :amount               amount
                                               :payment-method-nonce payment-method-authorization
                                               :merchant             (payment-entity->merchant payment-entity)
                                               :order-id             correlation}))]
    (if (bt-r/success? result)
      (m-either/right (bt-tx/to-map (bt-r/target result)))
      (m-either/left  {:error (bt-r/message result)}))))

(defn tx-sale [{:keys [customer-id amount payment-method-authorization payment-entity correlation] :as params}]
  (log/info "calling braintree transaction sale -> :customer-id" customer-id, ":correlation" correlation)
  (try
    (bt-tx-sale params)
    (catch Exception e
      (m-either/left {:stacktrace (capture-stack-trace e)}))))

(defn tx-void [{tx-id :gateway-id :as tx}]
  (log/info "void transaction [" tx-id  "]")
  (try
    (let [result (braintree/tx-void tx-id)]
      (if (bt-r/success? result)
        (m-either/right (bt-tx/to-map (bt-r/target result)))
        (m-either/left {:error (bt-r/message result)})))
    (catch Exception e
      (m-either/left {:stacktrace (capture-stack-trace e)}))))

(defn tx-refund [{id :id amount :amount :as tx}]
  (let [result (braintree/tx-refund id amount)]
    (if (bt-r/success? result)
      (m-either/right (bt-tx/to-map (bt-r/target result)))
      (m-either/left {:error (bt-r/message result)}))))



