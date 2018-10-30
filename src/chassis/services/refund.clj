(ns chassis.services.refund
  (:require [chassis.domains.session :as s]
            [chassis.domains.transaction :as t]

            [chassis.braintree :as bt]
            [chassis.failure :refer [failure]]
            [clojure.tools.logging :as log]
            [cats.monad.either :refer [right left either lefts branch-right]]
            [cats.core :refer [mlet bind fmap extract]]
            [chassis.domains.movement :as m]))

(defn mark-tx-as-error [type parent-id order amount response]
  (log/info "marking transaction as FAILED")
  (t/save (t/build  {:uuid (:uuid order)
                     :order-id (:id order)
                     :status "FAILED"
                     :provider "BRAINTREE"
                     :amount amount
                     :final-state true
                     :tx-parent-id parent-id
                     :type type
                     :response response}))
  (left (failure "payment.gateway.failed")))

(defn mark-tx-as-success [type parent-id  order amount response]
  (log/info "marking transaction as SUCCESS")
  (t/save (t/build  {:uuid       (:uuid order)
                     :order-id   (:id order)
                     :status     (:status response)
                     :type       type
                     :tx-parent-id parent-id
                     :provider   "BRAINTREE"
                     :final-state (:final-state response)
                     :amount      (or (:amount response) amount)
                     :gateway-id  (:id response)
                     :response    response})))

(defmulti command-executor (fn [_ _ { command :command }] command))

(defmethod command-executor :void [order session void]
  (log/info "creating :void transaction")
  (let [amount (:amount void)
        tx (:tx void)]
    (-> (bt/tx-void tx)
        (either (partial mark-tx-as-error "VOID" (:id tx) order amount)
                (partial mark-tx-as-success "VOID" (:id tx) order amount)))))

(defmethod command-executor :sale [order session {amount :amount tx :tx}]
  (log/info "creating :sale transaction")
  (-> (bt/tx-sale {:customer-id (:customer-id session)
                   :amount      amount
                   :correlation (:correlation session)
                   :payment-method-authorization (:payment-method-authorization order)
                   :payment-entity (:payment-entity order)})
      (either (partial mark-tx-as-error "SALE" nil order amount)
              (partial mark-tx-as-success "SALE" nil order amount))))

(defmethod command-executor :refund [order session {tx :tx}]
  (log/info "creating :refund transaction")
  (-> (bt/tx-refund {:id (:gateway-id tx) :amount (:refund-balance tx)})
      (either (partial mark-tx-as-error "REFUND" (:id tx) order (:refund-balance tx))
              (partial mark-tx-as-success "REFUND" (:id tx) order (:refund-balance tx)))))

(defmethod command-executor :default [order session command]
  (throw "STATE NOT DEFINED"))

(defn refund-tx [order session command]
  (future (command-executor order session command)))

(defn bt-refund-txs [order session txs]
  (right (doall (map (comp deref (partial refund-tx order session)) txs))))

(defn fmapl [fv f] (fmap f fv))

(defmulti build-movement (fn [{type :type}] (keyword (clojure.string/lower-case type))))

(defmethod build-movement :void [{order-id :order-id amount :amount tx-id :id}]
  (m/build-debit {:order-id order-id
                  :tx-id    tx-id
                  :amount   amount}))

(defmethod build-movement :refund [{order-id :order-id amount :amount tx-id :id}]
  (m/build-debit {:order-id order-id
                  :tx-id    tx-id
                  :amount   amount}))

(defmethod build-movement :sale [{order-id :order-id amount :amount tx-id :id}]
  (m/build-credit {:order-id order-id
                   :tx-id    tx-id
                   :amount   amount}))

(defn generate-movement [tx]
  (log/info "creating movement" tx)
  (m/save (build-movement tx)))

(defn generate-movements [order transactions]
  (log/info "starting movement generation for successfuly transactions")
  (let [movements (doall (map #(branch-right % generate-movement) transactions))]
    (if (empty? (lefts movements))
       (right movements)
       (left (failure "refund.transaction.failed")))))

(defn refund [session order]
  (log/info "initiating refund")
  (let [refund-amount (:amount order)]
    (->  (t/find-transactions-by-session (:id session))
         (bind t/positive-balance)
         (bind (partial t/eligible-for-refund refund-amount))
         (bind (partial t/generate-commands refund-amount))
         (fmapl :commands)
         (bind (partial bt-refund-txs order session))
         (bind (partial generate-movements order)))))