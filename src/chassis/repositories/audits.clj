(ns chassis.repositories.audits
  (:require [hugsql.core :as hugsql]
            [chassis.db :as db]))

(hugsql/def-db-fns "chassis/repositories/sql/audits.sql")


(defn fetch-by [table-name cond]
  (find-by {:datasource db/datasource}
           {:table-name table-name :cond cond}))
