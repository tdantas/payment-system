(ns chassis.domains.movement
  (:require [clojure.core :as clj]
            [chassis.http :refer [WebDTO]]
            [chassis.domains.transaction :as tx]
            [chassis.repositories.movements :as repo-movements]
            [cats.core :refer [foldl return]]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [chassis.failure :refer [wrap-try failure exception]]
            [cats.monad.either :refer [left right]]))

(defn dto [{:keys [id order-id amount type]}]
  {:id id :order-id order-id :amount amount :type type})

(defrecord Movement [id, order-id, amount, type, tx-id]
  WebDTO
 (-dto [this] (dto this)))

(defn build [{:keys [tx-id order-id amount type]}]
  {:pre  [(some? amount) (some? type) (some? type) (some? order-id)]}
  (map->Movement {:order-id order-id
                  :amount amount
                  :tx-id tx-id
                  :type type}))

(defn build-debit [{:keys [tx-id order-id amount] :as params}]
  {:pre  [(some? amount) (some? order-id)]}
  (build (assoc params :type "DEBIT")))

(defn build-credit [{:keys [tx-id order-id amount] :as params}]
  {:pre  [(some? amount) (some? order-id)]}
  (build (assoc params :type "CREDIT")))

(defn- snake->Movement [db-order]
  (map->Movement (transform-keys ->kebab-case-keyword db-order)))

(defn save [movement]
  (wrap-try "movement.save.failed"
    (snake->Movement (repo-movements/insert movement))))