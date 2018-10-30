(ns chassis.domains.orders.core
  (:require [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [chassis.failure :refer [wrap-try failure exception concat-failures]]
            [cats.monad.either :refer [lefts left right]]
            [chassis.domains.orders.protocol :as p]
            [chassis.domains.orders.sale]
            [chassis.domains.orders.refund]))

(defn build [params]
  (p/web->order params))

(defn mark-as-fullfiled [order]
  (p/-update order {:status "FULLFILED"}))

(defn mark-as-failed [order]
  (p/-update order {:status "FAILED"}))

(defn valid? [order]
  (p/-valid? order))

(defn save [order]
  (p/-save order))

(defn update [order params]
  (p/-update order params))