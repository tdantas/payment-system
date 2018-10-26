(ns chassis.repositories.transactions
  (:require [hugsql.core :as hugsql]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [chassis.db :as db]))

(hugsql/def-db-fns "chassis/repositories/sql/transactions.sql")

(defn db->transactions [db-transactions]
  (transform-keys ->kebab-case-keyword db-transactions))

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
   (db->transactions (insert-sale-tx db tx))))

(defn update
  ([id params]    (update {:datasource db/datasource} id params))
  ([db id params] (db->transactions (update-sale-tx db (merge params {:id id})))))

(defn find-by-session
  ([session-id] (find-by-session {:datasource db/datasource} session-id))
  ([db session-id] (find-txs-by-session-id db {:session-id session-id})))