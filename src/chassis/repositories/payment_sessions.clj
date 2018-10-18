(ns chassis.repositories.payment-sessions
 (:require [hugsql.core :as hugsql]
           [chassis.db :as db]
           [camel-snake-kebab.core   :refer [->kebab-case-keyword]]
           [camel-snake-kebab.extras :refer [transform-keys]]))

(hugsql/def-db-fns "chassis/repositories/sql/payment-sessions.sql")

(defn db->payment-session [db-ps]
  (transform-keys ->kebab-case-keyword db-ps))

(defn insert [{:keys [customer-id correlation client expiration-date]}]
  (insert-payment-session  {:datasource db/datasource}
                           {:customer-id customer-id
                            :client client
                            :expiration-date expiration-date
                            :correlation correlation}))
(defn find-by-id [id]
  (db->payment-session (find-unique {:datasource db/datasource} {:id id})))