(ns chassis.main
  (:require [chassis.config :refer [config]]
            [chassis.server :refer [server]]
            [chassis.db :refer [datasource]]
            [chassis.utils.logging :as ulogging]
            [clojure.tools.logging :as l]
            [mount.core :as mount])
  (:gen-class))

(set! *warn-on-reflection* 1)

(defn verify [& args]
  (chassis.config/init args))

(defn l [msg]
  (l/info "---------------------------------------------------")
  (l/info msg))

(defn init []
  (ulogging/with-logging-status)
  (mount/start-with-args (mount/args)))

(defn -main [& args]
  (ulogging/with-logging-status)
  (mount/start-with-args args))
