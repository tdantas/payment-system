(ns chassis.domains.orders.refund
  (:require [chassis.repositories.orders :as repo-order]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [chassis.http :refer [dto]]
            [chassis.failure :refer [wrap-try failure exception concat-failures validation-error]]
            [cats.monad.either :refer [lefts left right]]
            [chassis.domains.orders.protocol :refer [BaseOrder web->order]]))

(declare valid? save update)

(def REFUND "REFUND")

(defrecord RefundOrder [type, uuid, amount, created-at, session-id, payment-entity]
  BaseOrder
  (-valid? [order] (valid? order))
  (-save [order] (save order))
  (-update [order params] (update order params)))

(defn db->kebab-order [data]
  (map->RefundOrder (transform-keys ->kebab-case-keyword data)))

(defmethod dto RefundOrder [order]
  {:status (:status order)
   :id (:id order)
   :type "REFUND"
   :amount (:amount order)})

(defmethod web->order :refund [{session-id :session-id
                                uuid       :uuid
                                status     :status
                                amount     :amount
                                payment-entity :payment-entity}]

  (map->RefundOrder {:uuid            uuid
                     :amount          amount
                     :status         (or status "PENDING")
                     :session-id      session-id
                     :payment-entity payment-entity
                     :type           REFUND}))

(defn has-amount? [{amount :amount :as order}]
  (if (pos? amount)
    (right order)
    (left (failure "amount.required"))))

(defn valid? [^RefundOrder order]
  (let [errors (concat-failures [(has-amount? order)])]
    (if (empty? errors)
      (right order)
      (left (validation-error "refund.validation.failed" errors)))))

(defn save [^RefundOrder order]
  (wrap-try "refund.order.save.failed"
      (db->kebab-order (repo-order/insert-refund order))))

(defn update [^RefundOrder order update-params]
  (wrap-try "refund.order.update.failed"
      (db->kebab-order (repo-order/update order update-params))))