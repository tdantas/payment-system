(ns chassis.services.refund
  (:require [chassis.repositories.transactions :as repo-tx]
            [chassis.repositories.movements :as repo-mov]

            [cats.monad.exception :as m-exception]
            [cats.core :refer [>>= bind]]
            [cats.monad.either :refer [either]]

            [chassis.braintree :as bt]
            [clojure.tools.logging :as log]
            [cats.monad.either :as m-either]))

(defn balance [movements]
  (reduce (fn [acc {amount :amount type :type :as m}]
            (case type
              "CREDIT" (+ acc amount)
              "DEBIT"  (- acc amount)
              :else acc)) 0 movements))

(defn validate [amount movements]
  (let [total (balance movements)]
    (cond
      (>= total amount) (m-either/right movements)
      :else (m-either/left { :balance total :msg "insuficient balance"}))))

;(defn without-balance [{:keys [balance msg] :as error}]
;  (log/info "transaction sale has failed")
;  (let [params (assoc params :gateway-response error)]
;    (try
;      (m-either/right    (repo-tx/insert (merge params {:type "REFUND" :status "FAIL"})))
;      (catch Exception e (m-either/left {:exception e})))))


(defn braintree-refund [])

(defn refund [uuid {correlation :correlation id :id customer-id :customer-id :as session} {amount :amount}]
  (log/info "initiating refund" id)
  (-> (repo-mov/find-session-settled-movements id)
      (bind (partial validate amount))))
