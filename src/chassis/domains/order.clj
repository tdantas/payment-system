(ns chassis.domains.order
  (:require [chassis.repositories.orders :as repo-order]
            [clojure.core :as clj]
            [cats.core :refer [foldl return]]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [chassis.failure :refer [wrap-try failure exception concat-failures]]
            [cats.monad.either :refer [lefts left right]]
            [chassis.domains.session :as s]))

(def PAYMENT "PAYMENT")
(def REFUND  "REFUND")
(def VOID    "VOID")

(def types #{PAYMENT REFUND VOID})

(defrecord Order [id, type, items, uuid, amount, billable, created-at, session-id, payment-method, payment-entity, payment-method-authorization, currency])

(defn build [{session-id :id } {uuid :uuid
                                amount :amount
                                billable :billable
                                cost :cost
                                payment-method :payment-method
                                items :items
                                currency :currency
                                payment-method-authorization  :payment-method-authorization
                                payment-entity :payment-entity}]
  (map->Order {:uuid       uuid
               :billable   (or billable amount)
               :cost       (or cost 0)
               :items      items
               :type       PAYMENT
               :session-id session-id
               :amount     amount
               :payment-method-authorization payment-method-authorization
               :payment-entity payment-entity
               :currency   currency
               :payment-method payment-method}))

(defn billable [^Order order] (:billable order))
(defn customer-id [^Order order] (:customer-id (:session order)))
(defn amount [^Order order] (:amount order))
(defn session-id [^Order order] (:session-id order))
(defn uuid [^Order order] (:uuid order))
(defn id [^Order order] (:id order))
(defn items [^Order order] (:items order))
(defn type [^Order order] (:type order))

(defn db->kebab-order [data]
  (map->Order (transform-keys ->kebab-case-keyword data)))

(defn has-session? [{session-id :session-id :as order}]
  (if-not (nil? session-id)
    (right order)
    (left (failure "validation.order.session.required"))))

(defn has-uuid? [{uuid :uuid :as order}]
  (if-not (nil? uuid)
    (right order)
    (left (failure "validation.order.uuid.required"))))

(defn has-amount? [{amount :amount :as order}]
  (if-not (nil? amount)
    (right order)
    (left (failure "validation.order.amount.required"))))

(defn items-total-match-with-amount? [{amount :amount items :items :as order}]
  (let [equals? (= amount
                   (reduce (fn [total item] (+ total (:total item))) 0 items))]
    (if equals?
      (right order)
      (left (failure "validation.order.items.mismatch-total")))))

(defmulti valid? (fn [{type :type}] (keyword (clojure.string/lower-case type))))

(defmethod valid? :payment [^Order order]
  (let [errors (concat-failures [(has-session? order)
                                 (has-uuid? order)
                                 (has-amount? order)
                                 (items-total-match-with-amount? order)])]
    (if (empty? errors)
      (right order)
      (left errors))))


(defmethod valid? :default [o] (throw (Exception. (str "Invalid valid? call " (type o)))))

(defn map [coll]
  (clj/map db->kebab-order coll))

(defn save [^Order order]
  (wrap-try "order.save.failed"
    (db->kebab-order (repo-order/insert order))))


