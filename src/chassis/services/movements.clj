(ns chassis.services.movements
  (:require [chassis.repositories.movements :as repo-mov]
            [cats.monad.identity :as m-identity]))

(defn find-by-session [{id :id}]
  (repo-mov/find-movements-by-session id))