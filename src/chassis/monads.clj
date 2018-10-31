(ns chassis.monads
  (:require [cats.builtin]
            [cats.core :refer [fmap extract]]))

(defn tee [f]
  (fn [v]
      (f v)
      v))

(defn fmap-left [fv f] (fmap f fv))
