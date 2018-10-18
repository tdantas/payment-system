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
            [chassis.handlers.orders :as tx]
            [cats.core :as m-core]
            [chassis.repositories.orders :as orders-repo]

            [chassis.repositories.transactions :as tx-repo]
            [chassis.repositories.movements :as movements-repo]

            [chassis.domains.order :as o]
            [chassis.domains.transaction :as t]
            [chassis.domains.session :as s]

            [chassis.failure :as failure]

            [chassis.repositories.request-log :as rl]
            [cats.monad.either :as m-either]
            [cats.monad.exception :as m-exception]
            [chassis.repositories.movements :as repo-mov]
            [cats.monad.maybe :as maybe]
            [cheshire.generate :as protocol])
 (:import [chassis.failure Failure]))



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
;
;(def validators [[#(> %1 5), :gt-than-5] [odd?, :odd]])
;
;(defn validate [record, errors, [f key]]
;  (if (f record)
;    errors
;    (assoc errors :errors (conj (:errors errors) key))))
;
;(defn valid? [record validators]
;  (reduce (partial validate record) {:errors []} validators))
;
;(valid? 10 validators)

;(def new-session (s/build "841081114" {}))
;(def session (m-core/extract (s/save new-session)))
;session
;;;
;(def order (o/build
;             session {:payment-method "cc"
;                      :items {}
;                      :amount 0
;                      :cost 0
;                      :billable 0
 ;                      :payment-entity nil
;                      :type "PAYMENT"
;                      :uuid "3123123"}))
;
;
;(m-core/mlet [session (s/save new-session)]
;             session)
;;
;order
;(o/valid? order)
;;
;(def saved-order (cats.core/extract (o/save order)))
;saved-order
;;
;(cats.core/extract (failure/wrap-try "dsa"
;                                     (m-either/right (o/db->kebab-order (o/save order)))))

;
;(protocol/add-encoder Failure
;                      (fn [f jg]
;                        (cheshire.generate/write-string jg (:msg f))))
;;
;(cheshire.core/generate-string (chassis.failure/failure "hello"))
;;
;;(cheshire.core/generate-string (o/valid? order))
;
;(defn ccc [coll]
;  (reduce (fn [acc {left :left}]
;            (cons acc left)) [] (m-either/lefts coll)))
;
;(ccc [(m-either/left (failure/failure "a"))])
;
;(count (concat [] [(failure/failure "a")]))
;
;(let [{left :left} (m-either/left (failure/failure "abc"))]
;  (print left))

(merge-with (fn [x y] (println x y) {:a 1} {:a 2}))
;(repo-mov/insert-credit {:gateway-id "3123123" :amount "3232" :transaction-id 1 :status "SETTLED"})

