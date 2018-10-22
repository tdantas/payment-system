(ns chassis.domains.movement
  (:require [clojure.core :as clj]
            [chassis.repositories.movements :as repo-movements]
            [cats.core :refer [foldl return]]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [chassis.failure :refer [wrap-try failure exception]]
            [cats.monad.either :refer [left right]]))

(defrecord Movement [id, order-id, amount, type, status])

(defn build [{:keys [order-id amount type status]}]
  {:pre  [(some? amount) (some? type) (some? type) (some? order-id)]}
  (map->Movement {:order-id order-id
                  :amount amount
                  :type type
                  :status status}))

(defn build-credit [{order-id :id :as order} {:keys [amount status] :as params}]
  {:pre  [(some? amount) (some? type)  (some? order-id)]}
  (build (assoc params :order-id order-id :type "CREDIT")))

(defn build-debit [{order-id :id :as order} {:keys [amount status] :as params}]
  {:pre  [(some? amount) (some? type)  (some? order-id)]}
  (build (assoc params :order-id order-id :type "DEBIT")))

(defn- snake->Movement [db-order]
  (map->Movement (transform-keys ->kebab-case-keyword db-order)))

(defn save [movement]
  (wrap-try "movement.save.failed"
            (snake->Movement (repo-movements/insert movement))))

(defn credit [^Movement movement]
  (save (assoc movement :type "CREDIT")))

(defn debit [^Movement movement]
  (save (assoc movement :type "DEBIT")))