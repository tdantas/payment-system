(ns chassis.repositories.transactions
  (:require [hugsql.core :as hugsql]
            [chassis.db :as db]))

(hugsql/def-db-fns "chassis/repositories/sql/transactions.sql")

(defn insert
  ([params] (insert-tx {:datasource db/datasource} params))
  ([db {:keys [amount, billable, cost, items, status, payment_method, session_id] :as tx}] (insert-tx db tx)))

(defn update
  ([id params] (update {:datasource db/datasource} id params))
  ([db id params] (update-tx db (merge params {:id id}))))

