(ns chassis.services.sale
  (:require
        [chassis.domains.transaction :as t]
        [chassis.domains.movement :as m]

        [cats.core :refer [bind]]
        [chassis.failure :refer [exception failure]]
        [chassis.failure :refer [failure wrap-try]]

        [chassis.braintree :as bt]
        [clojure.tools.logging :as log]

        [cats.monad.either :refer [right left either]]))

(defn mark-tx-as-error [order gateway-response]
  (log/info "marking transaction as failed")
  (t/save (t/build order {:uuid (:uuid order)
                          :status "FAIL"
                          :amount (:amount order)
                          :type "SALE"
                          :response gateway-response}))

  (log/info "transaction marked as failed")
  (left (failure "movement.gateway.failed")))

(defn mark-tx-as-success [order response]
  (log/info "marking transaction as SUCCESS")
  (t/save (t/build order {:uuid      (:uuid order)
                          :status    (:status response)
                          :type      "SALE"
                          :amount    (:amount response)
                          :gateway-id (:id response)
                          :response response})))

(defn generate-movement [{amount :amount
                          order-id :order-id
                          status :status
                          :as tx}]

  (log/info "creating credit movement")
  (m/save (m/build-credit {:id order-id} { :status status
                                           :amount amount})))

(defn create-tx-and-movement [order bt]
  (-> (mark-tx-as-success order bt)
      (bind generate-movement)))

(defn sale [session order]
  (log/info "initiating sale for order"  (:id order) "session" (:id session))
  (-> (bt/tx-sale {:customer-id (:customer-id session)
                   :amount      (:amount order)
                   :correlation (:correlation session)
                   :payment-method-authorization (:payment-method-authorization order)
                   :payment-entity (:payment-entity order)})

     (either (partial mark-tx-as-error  order)
             (partial create-tx-and-movement order))))

