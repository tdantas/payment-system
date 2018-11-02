(ns chassis.http
  (:require [clojure.core.match :as pattern]
            [chassis.config :refer [config]]
            [clojure.stacktrace :as st]
            [ring.util.http-response :as resp]))

(defmulti dto type)
(defmethod dto :default [v] v)

(defn type->dto [f v]
  (let [dto-value (dto v)
        response (f dto-value)]
    (if (map? dto-value)
      (merge response {:status (or (:http-status-code dto-value) (:status response))})
      response)))

(defn translate [m]
  (pattern/match m
    {:e e}             (type->dto resp/internal-server-error e)
    {:v v}             (type->dto identity v)

    {:just j}          (type->dto resp/ok j)
    {:nothing _}       (type->dto resp/not-found {})

    {:left f}          (type->dto resp/precondition-failed f)
    {:right r}         (type->dto resp/ok r)

    {:seq s}           (type->dto resp/ok s)))