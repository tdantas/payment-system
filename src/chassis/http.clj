(ns chassis.http
  (:require [clojure.core.match :as pattern]
            [chassis.config :refer [config]]
            [clojure.stacktrace :as st]
            [chassis.failure :refer [exception]]
            [ring.util.http-response :as resp]))


(defrecord HttpFailure [code msg])

(defn failure->http [{code :code msg :msg}]
  (let [code (or code "ERROR")]
    (if (coll? msg)
      (map->HttpFailure {:code code :msg (map failure->http msg)})
      (map->HttpFailure {:code code :msg msg}))))

(defn internal-server-error [e]
  (condp (:env config) =
    "development" (do (st/print-stack-trace e) (resp/internal-server-error e))
    (resp/internal-server-error (failure->http (exception e)))))

(defn translate [m]
  (pattern/match m
    {:e e}             (internal-server-error e)
    {:v v}             (resp/ok v)

    {:just j}          (resp/ok j)
    {:nothing _}       (resp/not-found)

    {:left f}          (resp/unprocessable-entity (failure->http  f))
    {:right r}         (resp/ok r)))