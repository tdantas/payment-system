(ns chassis.domains.orders.protocol
  (:require [clojure.tools.logging :as log]))

(defprotocol BaseOrder
  (-valid? [order] "validate the order")
  (-save [order] "save order")
  (-update [order params]))


(defn dispatcher [params]
  (log/info "dispatching order to" (:type params))
  (:type params))

(defmulti web->order dispatcher)

(defmethod web->order :default [params]
  (throw (str "invalid order type [" (:type params) "]")))
