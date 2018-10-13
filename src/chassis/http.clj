(ns chassis.http
  (:require [clojure.core.match :as pattern]
            [chassis.config :refer [config]]
            [ring.util.http-response :as resp]))

(defn msg [code message]
  {:code code :message message})

(defn internal-server-error [e]
  (condp (:env config) =
    "development" (resp/internal-server-error e)
    (resp/internal-server-error (msg "INTERNAL_SERVER_ERROR" "Internal Server Error"))))

(defn translate [m]
  (pattern/match m
    {:e e}       (internal-server-error e)
    {:v v}       (resp/ok v)

    {:just j}    (resp/ok j)
    {:nothing _} (resp/not-found)

    {:left l}    (resp/unprocessable-entity (msg 422 l))
    {:right r}   (resp/ok r)))