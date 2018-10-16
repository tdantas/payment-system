(ns chassis.repositories.movements
  (:require [hugsql.core :as hugsql]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [chassis.db :as db]
            [cats.monad.maybe :refer [seq->maybe]]))

(hugsql/def-db-fns "chassis/repositories/sql/movements.sql")

(defn db->movements [db-movements]
  (transform-keys ->kebab-case-keyword db-movements))

(defn- default [params]
  (merge {:movement_parent_id nil
          :gateway-id nil} params))

(defn insert
  ([params]     (insert {:datasource db/datasource} params))
  ([db params]  (db->movements (insert-movement db (default params)))))

(defn insert-credit
  ([params]    (insert-credit {:datasource db/datasource} params))
  ([db params] (insert db (assoc params :type "CREDIT"))))

(defn insert-debit
  ([params]    (insert-debit {:datasource db/datasource} params))
  ([db params] (insert db (assoc params :type "DEBIT"))))

(defn find-movements-by-session
  ([id]    (find-movements-by-session {:datasource db/datasource} id))
  ([db id] (cats.monad.identity/identity (map db->movements (movements-by-session db {:session-id id})))))

(defn find-session-settled-movements
  ([id]    (find-session-settled-movements {:datasource db/datasource} id))
  ([db id] (cats.monad.identity/identity (map db->movements
                                              (movements-by-session-and-status db {:status "SETTLED"
                                                                                   :session-id id})))))


