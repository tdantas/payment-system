(ns chassis.services.intent
  (:require [clojure.java.jdbc :refer [with-db-transaction]]
            [chassis.repositories.transactions :as repo-tx]
            [cats.monad.identity :as m-identity]
            [cats.monad.exception :as m-exception]
            [cats.core :as m-core]
            [clojure.stacktrace :as st]

            [braintree-clj.core :as braintree]
            [braintree-clj.transaction-request :as bt-tr]
            [braintree-clj.result :as bt-r]
            [braintree-clj.transaction :as bt-tx]
            [clojure.tools.logging :as log]
            [cats.monad.either :as m-either]))

(defn- update-intent [id {:keys [status gateway-response] :as params}]
  (log/info "updating intent" params)
  (repo-tx/update id params))

(defn- item-valid? [{:keys [qty total unit]}]
  (= (* qty unit) total))

(defn total-match-with-items? [items amount]
  (= amount
      (reduce (fn [total item] (+ total (:total item))) 0 items)))

(defn- items-valid? [items]
  (or (empty? items) (every? item-valid? items)))

(defn validate-payment-items [{:keys [items amount] :as tx}]
  (cond
    (not (total-match-with-items? items amount)) (m-either/left "Items and total does not match")
    (not (items-valid? items)) (m-either/left "Invalid transactions items")
    :else (m-either/right tx)))

(defn update-params [uuid payment-session params]
  (assoc params
    :session-id (:id payment-session)
    :uuid uuid))

(defn payment-entity->merchant [payment-entity]
  "runtime")

(defn bt-tx-request [{:keys [payment-method-nonce customer-id amount order-id merchant]}]
  (bt-tr/create {:customer-id           customer-id
                 :amount                amount
                 :merchant-account-id   merchant
                 :submit-for-settlement true
                 :payment-method-nonce  payment-method-nonce
                 :order-id              order-id}))

(defn bt-tx-sale [{:keys [customer-id amount payment-method-authorization payment-entity correlation]}]
  (log/info "calling braintree transaction sale -> :customer-id" customer-id, ":correlation" correlation)

  (braintree/tx-sale (bt-tx-request {:customer-id customer-id
                                       :amount amount
                                       :payment-method-nonce payment-method-authorization
                                       :merchant (payment-entity->merchant payment-entity)
                                       :order-id correlation})))

(defn capture-stack-trace [e]
  (with-out-str
    (st/print-stack-trace e)))

(defn update-bt-call [{:keys [id] :as params}]
      (try
        (let [result (bt-tx-sale params)
              tx (bt-r/target result)]

          (cond
            (bt-r/success? result) (m-either/right (update-intent id  {:status           (bt-tx/status tx)
                                                                       :gateway-response (bt-tx/to-map tx)}))

            :else                  (m-either/left  (update-intent id  {:status "FAIL"
                                                                       :gateway-response {:failure (bt-r/message result)}}))))
        (catch Exception e
             (m-either/left (update-intent id {:status "ERROR" :gateway-response {:failure (capture-stack-trace e)}})))))

(defn- insert-intent [intent-type params]
  (log/info "saving transaction" intent-type params)
  (repo-tx/insert (merge params {:type intent-type :status "NEW"})))

(def sale-intent (m-exception/wrap (partial insert-intent "sale")))

(defn register-intent [uuid payment-session params]
  (log/info "sale intent started with params: " params)
  (let [ident  (m-identity/identity (update-params uuid payment-session params))
        v      (m-core/bind ident validate-payment-items)
        result (m-core/bind v sale-intent)]
    result))

(defn call-braintree [params transaction]
  (update-bt-call (assoc params :id (:id transaction))))

(defn sale [uuid payment-session params]
  (let [transaction  (register-intent uuid payment-session params)
        params       (assoc
                       params
                       :customer-id (:customer-id payment-session)
                       :correlation (:correlation payment-session))]
       (m-core/bind  transaction (partial call-braintree params))))
