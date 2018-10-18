(ns chassis.services.payment-sessions
  (:require [chassis.domains.session :as s]
            [chassis.braintree :as bt]))

(defn create [params]
 (let [customer (bt/customer-create params)]
   (s/save (s/build (:id customer) params))))
