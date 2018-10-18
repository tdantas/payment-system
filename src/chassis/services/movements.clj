(ns chassis.services.movements
  (:require [chassis.repositories.movements :as repo-mov]))

(defn find-by-session [{id :id}]
  (repo-mov/find-movements-by-session id))