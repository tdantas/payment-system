(ns chassis.services.payment-sessions
 (:require [chassis.repositories.payment-sessions :as ps]))


(defn create [customer-id correlation]
  (ps/create customer-id correlation))