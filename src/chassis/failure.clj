(ns chassis.failure
  (:require [cats.monad.exception :as mexception]
            [cats.monad.either :refer [lefts left right]]
            [clojure.core.match :as pattern]
            [clojure.stacktrace :as st]
            [clojure.tools.logging :as log]))

(defrecord Failure [msg code ^Throwable exception])

(defn exception
  ([^Throwable e] (exception e (.getMessage e)))
  ([^Throwable e msg] (map->Failure {:exception e :msg msg :code "INTERNAL_ERROR"})))


(defn failure
  ([msg] (map->Failure {:msg msg}))
  ([msg code] (map->Failure {:msg msg :code code})))

(defn validation-failure [msg]
  (failure msg "VALIDATION"))

(defn append [error msg]
  (let [e-msg (:msg error)]
    (if (coll? e-msg)
      (conj e-msg msg)
      (vector e-msg msg))))

(defn capture-stack-trace [e]
  (with-out-str
    (st/print-stack-trace e)))

(defmacro wrap-try [msg & body]
  `(try
     (right (do ~@body))
     (catch Exception e#
       (do
           (log/info (.getMessage e#))
           (clojure.stacktrace/print-stack-trace e#)
           (left (exception e# ~msg))))))

(defn extract-failure [m]
  (pattern/match m
                 {:e e} e
                 {:left l} l
                 _ nil))

(defn concat-failures [coll]
  (reduce
    (fn [acc m]
      (let [e (extract-failure m)]
        (if e (cons e acc) acc)))
    [] coll))