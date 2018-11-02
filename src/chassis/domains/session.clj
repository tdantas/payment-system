(ns chassis.domains.session
  (:require [clojure.core :as clj]
            [chassis.repositories.payment-sessions :as repo-session]
            [cats.core :refer [foldl return]]
            [chassis.http :refer [WebDTO]]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [chassis.failure :refer [wrap-try failure exception]]
            [cats.monad.either :refer [left right]]))

(defn dto [session]
  {:id (:id session)
   :currency (:currency session)
   :customer-id (:customer-id session)})

(defrecord Session [id, correlation, customer-id, currency]
  WebDTO
  (-dto [this] (dto this)))

(defn build [customer-id {:keys [correlation currency client]}]
  (map->Session {:correlation correlation
                 :currency currency
                 :client client
                 :customer-id customer-id}))

(defn- snake->Session [db-order]
  (map->Session (transform-keys ->kebab-case-keyword db-order)))

(defn find-by-id [id]
  (if-let [session (repo-session/find-by-id id)]
    (right (snake->Session session))
    (left (failure "session.not-found"))))

(defn save [session]
  (wrap-try "session.save.failed"
    (snake->Session (repo-session/insert session))))


