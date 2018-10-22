(ns chassis.repositories.movements
  (:require [hugsql.core :as hugsql]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [chassis.db :as db]
            [chassis.failure :refer [wrap-try]]
            [cats.monad.maybe :refer [seq->maybe]]))

(hugsql/def-db-fns "chassis/repositories/sql/movements.sql")

(defn db->movements [db-movements]
  (transform-keys ->kebab-case-keyword db-movements))

(defn insert
  ([movement]     (insert {:datasource db/datasource} movement))
  ([db movement]   (db->movements (insert-movement db movement))))

(defn find-movements-by-session
  ([id]    (find-movements-by-session {:datasource db/datasource} id))
  ([db id] (map db->movements (movements-by-session db {:session-id id}))))

(defn find-session-settled-movements
  ([id]    (find-session-settled-movements {:datasource db/datasource} id))
  ([db id] (map db->movements  (movements-by-session-and-status db {:status "SETTLED" :session-id id}))))


