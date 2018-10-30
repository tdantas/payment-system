(ns chassis.domains.orders.sale
  (:require [chassis.repositories.orders :as repo-order]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [chassis.failure :refer [wrap-try failure exception concat-failures validation-failure]]
            [cats.monad.either :refer [lefts left right]]
            [chassis.domains.session :as s]
            [chassis.domains.orders.protocol :refer [BaseOrder web->order]]))

(declare valid? save update)

(def SALE "SALE")

(defrecord SaleOrder [id, type, items, uuid, amount, created-at, session-id, payment-method, payment-entity, payment-method-authorization, currency, status]
  BaseOrder
  (-valid? [order] (valid? order))
  (-save [order] (save order))
  (-update [order params] (update order params)))

(defn db->kebab-order [data]
  (map->SaleOrder (transform-keys ->kebab-case-keyword data)))

(defmethod web->order :sale [{session-id :session-id
                              uuid :uuid
                              amount :amount
                              status :status
                              billable :billable
                              cost :cost
                              payment-method :payment-method
                              items :items
                              currency :currency
                              payment-method-authorization  :payment-method-authorization
                              payment-entity :payment-entity}]

  (map->SaleOrder {:uuid       uuid
                   :billable   (or billable amount)
                   :cost       (or cost 0)
                   :items      items
                   :status     (or status "PENDING")
                   :type       SALE
                   :session-id session-id
                   :amount     amount
                   :payment-method-authorization payment-method-authorization
                   :payment-entity payment-entity
                   :currency        currency
                   :payment-method payment-method}))

(defn has-session? [{session-id :session-id :as order}]
  (if-not (nil? session-id)
    (right order)
    (left (validation-failure "validation.order.session.required"))))

(defn has-uuid? [{uuid :uuid :as order}]
  (if-not (nil? uuid)
    (right order)
    (left (validation-failure "validation.order.uuid.required"))))

(defn has-amount? [{amount :amount :as order}]
  (if-not (nil? amount)
    (right order)
    (left (validation-failure "validation.order.amount.required"))))

(defn items-total-match-with-amount? [{amount :amount items :items :as order}]
  (let [equals? (= amount
                   (reduce (fn [total item] (+ total (:total item))) 0 items))]
    (if equals?
      (right order)
      (left (validation-failure "validation.order.items.mismatch-total")))))

(defn valid? [^SaleOrder order]
  (let [errors (concat-failures [(has-session? order)
                                 (has-uuid? order)
                                 (has-amount? order)
                                 (items-total-match-with-amount? order)])]
    (if (empty? errors)
      (right order)
      (left (failure errors)))))

(defn save [^SaleOrder order]
  (wrap-try "sale.order.save.failed"
            (db->kebab-order (repo-order/insert-sale order))))

(defn update [^SaleOrder order update-params]
  (wrap-try "sale.order.update.failed"
            (db->kebab-order (repo-order/update order update-params))))