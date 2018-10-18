(ns chassis.domains.transaction
  (:require [clojure.core :as clj]
            [cats.core :refer [foldl return]]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [chassis.failure :refer [wrap-try failure exception]]
            [cats.monad.either :refer [left right]]
            [chassis.repositories.transactions :as rtx]))


(defrecord Transaction [id order-id amount status response created-at gateway-id uuid type])

(defn build [{order-id :id :as order } {:keys [uuid status amount response gateway-id]}]
  (map->Transaction {:order-id order-id
                     :uuid uuid
                     :status status
                     :response response
                     :amount amount
                     :gateway-id gateway-id}))


(defn- snake->Transaction [db-order]
  (map->Transaction (transform-keys ->kebab-case-keyword db-order)))

(defn save [^Transaction transaction]
  (wrap-try "transaction.save.failed"
    (snake->Transaction (rtx/insert transaction))))





