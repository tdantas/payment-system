(ns chassis.http
  (:require [clojure.core.match :as pattern]
            [chassis.config :refer [config]]
            [clojure.stacktrace :as st]
            [chassis.failure :refer [exception]]
            [ring.util.http-response :as resp]))


(defrecord HttpFailure [code msg])

(defn failure->http [{code :code msg :msg :as t}]
  (let [code (or code  t "ERROR")]
    (if (coll? msg)
      (map->HttpFailure {:code code :msg (map failure->http msg)})
      (map->HttpFailure {:code code :msg msg}))))

(defn exception-or-unprocessable-entity [{e :exception  msg :msg code :code}]
  (if e
    (resp/internal-server-error (failure->http {:code "INTERNAL_SERVER_ERROR", :msg (.getMessage e)}))
    (resp/unprocessable-entity  (failure->http {:msg msg :code code}))))


(defn translate [m]
  (pattern/match m
    {:e e}             (resp/internal-server-error e)
    {:v v}             (resp/ok v)

    {:just j}          (resp/ok j)
    {:nothing _}       (resp/not-found)

    {:left f}          (exception-or-unprocessable-entity f)
    {:right r}         (resp/ok r)))