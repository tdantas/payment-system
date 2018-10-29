(ns chassis.server
  (:require [chassis.config :refer [config]]
        [chassis.handler :as handler]
        [mount.core :refer [defstate]]
        [cats.builtin]
        [ring.adapter.jetty :refer [run-jetty]]))

(defstate server 
  :start (run-jetty handler/app {:port   (:server_port config) 
                                 :async? false
                                 :join?  false})
  :stop  (.stop server))
  
