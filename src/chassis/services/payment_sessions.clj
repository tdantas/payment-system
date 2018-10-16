(ns chassis.services.payment-sessions
  (:require [chassis.repositories.payment-sessions :as ps]
            [cats.monad.exception :as m-exception]
            [cats.monad.maybe :as m-maybe]

            [chassis.braintree :as bt]))

(defn create [params]
  (m-exception/try-on
    (let [customer (bt/customer-create params)]
      (ps/create (assoc params :customer-id (:id customer))))))

(defn find-session [id]
  (if-let [session (ps/find-by-id id)]
    (m-maybe/just session)
    (m-maybe/nothing)))