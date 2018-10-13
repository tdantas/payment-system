(ns user
  (:require [clojure.pprint :refer [pprint]]
            [mount.core :as mount]
            [mount.tools.graph :refer [states-with-deps]]
            [clojure.tools.namespace.repl :as tn]
            [chassis.utils.logging :refer [with-logging-status]]
            [chassis.config :refer [config]]
            [chassis.server :refer [server]]
            [chassis.repositories.payment-sessions :as purchase-orders]
            [braintree-clj.core :as core]
            [chassis.services.payment-sessions :as svc-po]
            [chassis.repositories.audits :as ra]
            [chassis.handlers.transactions :as tx]
            [cats.core :as m-core]
            [chassis.repositories.transactions :as tx-repo]
            [chassis.repositories.payment-sessions :as session-repo]

            [cats.monad.either :as m-either]
            [cats.monad.exception :as m-exception]))


              

(defn start []
  (with-logging-status)
  (mount/start-with-args *command-line-args*))

(defn stop []
  (mount/stop))

(defn refresh []
  (stop)
  (tn/refresh))

(defn refresh-all []
  (stop)
  (tn/refresh-all))

(defn go
  "starts all states defined by defstate"
  []
  (start)
  :ready)

(defn reset
  "stops all states defined by defstate, reloads modified source files, and restarts the states"
  []
  (stop)
  (tn/refresh :after 'user/go))

(mount/in-clj-mode)

(start)
;; (svc-po/register "2
;;3198312" "description")
;
;(session-repo/create {:customer-id 10 :client {:email "thiago"}})
;;;
;(tx-repo/insert {:items nil :session-id 1
;                 :amount 10, :billable 10, :cost 10, :payment-method "cc", :status "new" :gateway-response {:exception "error"}})
;;
;(tx-repo/update 1 {:status "failed" :gateway-response {:success " ueo"}})

;(m-core/bind (m-either/right "hello") (comp m-core/return (fn [s] s)))