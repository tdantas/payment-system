(ns chassis.repositories.orders
  (:require [hugsql.core :as hugsql]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [chassis.db :as db]))

(hugsql/def-db-fns "chassis/repositories/sql/orders.sql")

(defn insert-sale [order]
    (insert-sale-order {:datasource db/datasource} order))

(defn update [order params]
  (update-order {:datasource db/datasource}
          (merge {:id (:id order)} params)))

(defn insert-refund [order]
  (insert-refund-order {:datasource db/datasource} order))