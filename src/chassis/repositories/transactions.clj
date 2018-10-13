(ns chassis.repositories.transactions
  (:require [hugsql.core :as hugsql]
            [chassis.db :as db]))

(hugsql/def-db-fns "chassis/repositories/sql/transactions.sql")

(defn insert
  ([params] (insert {:datasource db/datasource} params))
  ([db {:keys [uuid
               amount
               billable
               cost
               type
               items
               status
               payment_method
               session_id] :or {items nil, cost 0} :as tx}]
   (insert-sale-tx db tx)))

(defn update
  ([id params] (update {:datasource db/datasource} id params))
  ([db id params] (update-sale-tx db (merge params {:id id}))))

