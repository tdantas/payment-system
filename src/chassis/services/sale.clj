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
  (log/info "marking transaction as failed"
   (t/save (t/build  {:uuid (:uuid order)
                      :order-id (:id order)
                      :status "FAILED"
                      :provider "BRAINTREE"
                      :amount (:amount order)
                      :final-state true
                      :type "SALE"
                      :response gateway-response})))
  (log/info "transaction marked as failed")
  (left (failure "payment.gateway.failed")))

(defn mark-tx-as-success [order gateway-response]
    (log/info "marking transaction as SUCCESS")
    (t/save (t/build  {:uuid      (:uuid order)
                       :order-id   (:id order)
                       :status    (:status gateway-response)
                       :type      "SALE"
                       :provider   "BRAINTREE"
                       :final-state (:final-state gateway-response)
                       :amount    (:amount gateway-response)
                       :gateway-id (:id gateway-response)
                       :response gateway-response})))

(defn generate-movement [{amount :amount
                          order-id :order-id
                          id :id
                          :as tx}]

  (log/info "creating credit movement")
  (m/save (m/build-credit  {:order-id order-id
                            :tx-id id
                            :amount amount})))

(defn create-tx-and-movement [order bt]
  (-> (mark-tx-as-success order bt)
      (bind generate-movement)))

(defn sale [session order]
  (log/info "initiating sale for order" (:id order) "session" (:id session))
  (-> (bt/tx-sale {:customer-id (:customer-id session)
                   :amount      (:amount order)
                   :correlation (:correlation session)
                   :payment-method-authorization (:payment-method-authorization order)
                   :payment-entity (:payment-entity order)})

     (either (partial mark-tx-as-error  order)
             (partial create-tx-and-movement order))))

