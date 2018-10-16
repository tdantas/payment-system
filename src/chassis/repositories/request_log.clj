(ns chassis.repositories.request-log
  (:require [hugsql.core :as hugsql]
            [chassis.db :as db]
            [camel-snake-kebab.core   :refer [->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]))

(hugsql/def-db-fns "chassis/repositories/sql/request_log.sql")

(defn db->request-log [data]
  (transform-keys ->kebab-case-keyword data))

(defn insert [{:keys [uuid, url, method, payload] :as params}]
  (db->request-log
    (insert-request-log {:datasource db/datasource} params)))