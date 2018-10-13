(ns chassis.services.intent
  (:require [clojure.java.jdbc :refer [with-db-transaction]]
            [chassis.repositories.transactions :as repo-tx]
            [cats.monad.identity :as m-identity]
            [cats.monad.exception :as m-exception]
            [cats.core :as m-core]

            [braintree-clj.core :as braintree]
            [braintree-clj.transaction-request :as bt-tr]
            [braintree-clj.result :as bt-r]
            [braintree-clj.transaction :as bt-tx]
            [clojure.tools.logging :as log]
            [cats.monad.either :as m-either]))

(defn- update-intent [id {:keys [status gateway-response] :as params}]
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

(defn update-params-with-session [payment-session params]
  (assoc params :session-id (:id payment-session)))

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
    (braintree/tx-sale (bt-tx-request {:customer-id customer-id
                                       :amount amount
                                       :payment-method-nonce payment-method-authorization
                                       :merchant (payment-entity->merchant payment-entity)
                                       :order-id correlation})))

(defn update-bt-call [{:keys [id] :as params}]
      (try
        (let [result (bt-tx-sale params)
              tx (bt-r/target result)]

          (cond
            (bt-r/success? result) (m-either/right (update-intent id  {:status (bt-tx/status tx) :gateway-response (bt-tx/to-map tx)}))
            :else                  (m-either/left  (update-intent id  {:status "FAIL" :gateway-response (bt-r/errors tx)}))))
        (catch Exception e
             (m-either/left (update-intent id {:status "ERROR" :gateway-response {:exception (.getMessage e)}})))))

(defn- insert-intent [intent-type params]
  (repo-tx/insert (merge params {:type intent-type :status "new"})))

(def sale-intent (m-exception/wrap (partial insert-intent "sale")))

(defn register-intent [payment-session params]
  (log/info "sale intent started with params: " params)
  (println params)
  (let [ident  (m-identity/identity (update-params-with-session payment-session params))
        v      (m-core/bind ident validate-payment-items)
        result (m-core/bind v sale-intent)]
    result))

(defn call-braintree [params transaction]
  (update-bt-call (assoc params :id (:id transaction))))

(defn sale [payment-session params]
  (let [transaction  (register-intent payment-session params)
        params       (assoc params :customer-id (:customer-id payment-session))]
       (m-core/bind  transaction (partial call-braintree params))))



