(ns chassis.services.session
  (:require [chassis.domains.transaction :as t]
            [cats.core :refer [bind mlet]]
            [chassis.braintree :as bt]
            [cats.monad.either :refer [right]]
            [chassis.domains.session :as s]))

(defn calculate-amount [txs]
  (right {:balance (reduce #(+ %1 (:available-balance %2)) 0 txs)}))

(defn balance [session]
  (-> (t/find-transactions-by-session (:id session))
      (bind t/positive-balance)
      (bind calculate-amount)))

(defn create [params]
  (mlet [customer (bt/customer-create params)]
    (s/save (s/build (:id customer) params))))