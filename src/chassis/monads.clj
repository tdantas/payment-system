(ns chassis.monads
  (:require [cats.core :as core]))


(defn tee [f]
  (fn [v]
    (f v)
    v))