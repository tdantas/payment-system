(ns chassis.repositories.orders
  (:require [hugsql.core :as hugsql]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [chassis.db :as db]))

(hugsql/def-db-fns "chassis/repositories/sql/orders.sql")

(defn insert [order]
  (insert-order {:datasource db/datasource} order))
