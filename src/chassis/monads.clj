(ns chassis.monads
  (:require [cats.core :refer [fmap]]))


(defn tee [f]
  (fn [v]
    (f v)
    v))


(defn fmap-left [fv f] (fmap f fv))