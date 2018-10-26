(ns chassis.services.refund
  (:require [chassis.domains.session :as s]
            [chassis.domains.transaction :as t]

            [chassis.braintree :as bt]
            [clojure.tools.logging :as log]
            [cats.monad.either :refer [right left]]
            [cats.core :refer [mlet bind fmap]]))

(defmulti bt-refund (fn [{status :status}] (keyword status)))

(defmethod bt-refund :SUBMITTED_FOR_SETTLEMENT [tx]
  (bt/tx-void tx))

(defmethod bt-refund :AUTHORIZED [tx]
  (bt/tx-void tx))

(defmethod bt-refund :SETTLED [tx]
  (bt/tx-refund tx))

(defmethod bt-refund :default [tx]
  (throw "STATE NOT DEFINED"))

(defn refund-tx [{:keys [id gateway-id refund-balance status] :as tx}]
  (future (bt-refund tx)))

(defn bt-refund-txs [txs]
  (map (comp deref refund-tx) txs))

(defn fmap-left [fv f] (fmap f fv))

(defn refund [session order]
  (log/info "initiating refund")
  (let [refund-amount (:amount order)]
    (->  (t/find-transactions-by-session (:id session))
         (bind t/positive-balance)
         (bind (partial t/eligible-for-refund refund-amount))
         (bind (partial t/generate-commands refund-amount))
         (fmap-left :commands))))



