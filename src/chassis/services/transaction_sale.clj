(ns chassis.services.transaction-sale
  (:require [chassis.repositories.transactions :as repo-tx]
            [chassis.repositories.movements :as repo-mov]

            [cats.monad.exception :as m-exception]
            [cats.core :refer [>>= bind]]

            [chassis.braintree :as bt]
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
    (not (items-valid? items))                   (m-either/left "Invalid transactions items")
    :else (m-either/right tx)))

(defn mark-tx-as-error [type params bt-result]
  (log/info "transaction sale has failed")
  (let [params (assoc params :gateway-response bt-result)]
    (try
      (m-either/right    (repo-tx/insert (merge params {:type type :status "FAIL"})))
      (catch Exception e (m-either/left {:exception e})))))

(defn mark-tx-as-success [type params gateway-tx]
  (log/info "transaction sale has succeed")
  (let [status      (:status gateway-tx)
        params      (assoc params :gateway-response gateway-tx)]
    (try
      (m-either/right    (repo-tx/insert (merge params {:type type :status status})))
      (catch Exception e (m-either/left {:exception e})))))

(defn generate-movement [{amount :amount
                          tx-id :id
                          status :status
                          {gateway-id :id} :gateway-response
                          :as tx}]
  (log/info "creating credit movement" tx)
  (m-exception/try-on
    (repo-mov/insert-credit { :gateway-id     gateway-id
                              :status         status
                              :transaction-id tx-id
                              :amount         amount})))

(defn braintree-sale [params]
  (-> (bt/tx-sale params)
      (m-either/either (partial mark-tx-as-error   "SALE" params)
                       (partial mark-tx-as-success "SALE" params))))

(defn sale [uuid session params]
  (log/info "initiating sale for session" (:id session))
  (let [params (assoc params :session-id (:id session)
                             :customer-id (:customer-id session)
                             :order-id (:correlation session)
                             :uuid uuid)]

    (-> (validate-payment-items params)
        (bind braintree-sale)
        (bind generate-movement))))