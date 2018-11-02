(ns chassis.failure
  (:require [cats.monad.either :refer [lefts left right]]
            [clojure.core.match :as pattern]
            [clojure.stacktrace :as st]
            [chassis.http :refer [dto]]
            [clojure.tools.logging :as log]))

(defn capture-stack-trace [e]
  (with-out-str
    (st/print-stack-trace e)))

(defrecord Failure [msg])
(defrecord ValidationError [msg errors])
(defrecord AppError [msg ^Throwable exception stacktrace])

(defmethod dto Failure [{msg :msg}]
  {:type :failure
   :msg msg
   :http-status-code 422})

(defmethod dto AppError [{msg :msg st :stacktrace}]
  {:type :error
   :msg msg
   :stacktrace st
   :http-status-code 500})

(defmethod dto ValidationError [{msg :msg errors :errors}]
  {:type :validation
   :msg msg
   :errors errors
   :http-status-code 412})

(defn validation-error [msg errors]
  (map->ValidationError {:msg msg :errors errors}))

(defn exception
  ([^Throwable e] (exception e (.getMessage e)))
  ([^Throwable e msg] (map->AppError {:exception e :msg msg :stacktrace (capture-stack-trace e)})))

(defn failure
  ([msg] (map->Failure {:msg msg}))
  ([msg code] (map->Failure {:msg msg :code code})))

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
      (if-let [e (extract-failure m)]
        (cons e acc) acc))
    [] coll))

