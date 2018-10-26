(ns user
  (:require [clojure.pprint :refer [pprint]]
            [mount.core :as mount]
            [mount.tools.graph :refer [states-with-deps]]
            [clojure.tools.namespace.repl :as tn]
            [chassis.utils.logging :refer [with-logging-status]]
            [chassis.config :refer [config]]
            [chassis.server :refer [server]]
            [braintree-clj.core :as core]
            [chassis.handlers.orders :as tx]
            [cats.core :as m-core]

            [chassis.domains.order :as o]
            [chassis.domains.transaction :as t]
            [chassis.domains.session :as s]
            [chassis.domains.movement :as m]

            [chassis.repositories.orders :as orders-repo]
            [chassis.repositories.transactions :as tx-repo]
            [chassis.repositories.movements :as movements-repo]
            [chassis.repositories.request-log :as rl]

            [chassis.failure :as failure]

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
;(def f (m/save (m/build-credit {:id 10} {:amount 10 :type "CREDIT" :status "SETTLED"})))

;
;(defn txs-map [coll] (zipmap (map :id coll) coll))
;
;(defn find-genesis [txs-map id]
;  (loop [id-tx id]
;    (let [tx (get txs-map id-tx)
;          pid (:parent_id tx)]
;      (if (nil? pid)
;        (:id tx)
;        (recur pid)))))
;
;(defn save-into-txs-map [index acc key value]
;  (let [genesis (find-genesis index key)
;        tx  (get index genesis)
;        tx-descendants (:descendants tx [])]
;    (if (= key genesis)
;      (assoc acc genesis tx)
;      (assoc acc genesis
;                 (assoc tx :descendants
;                           (conj (get-in acc [genesis :descendants] []) value))))))
;
;
;(defn tx-descendants [txs]
;  (let [idx-tx (txs-map txs)]
;    (vals (reduce-kv(partial save-into-txs-map idx-tx) {} idx-tx))))
;
;
;(defn tx-real-amount [tx]
;  (let [amount (:amount tx)]
;    (case (:type tx)
;      "SALE"    amount
;      "REFUND"  (- amount)
;      "DEPOSIT" 0
;      "VOID"    (- amount)
;      (- amount))))
;
;
;(defn calculate-balance-tx [tx]
;  (let [tx-descendants (:descendants tx)
;        tx-balance (tx-real-amount tx)
;        tx-descendants-balance (map tx-real-amount tx-descendants)]
;    (apply + (conj tx-descendants-balance tx-balance))))
;
;
;(defn tx-balance-reducer [acc tx]
;    (conj acc (assoc tx :balance (calculate-balance-tx tx))))
;
;(defn tx-balance [txs]
;  (reduce  tx-balance-reducer [] (tx-descendants txs)))
;
;(def txs [{:id 1 :amount 100 :parent_id nil :type "SALE"}])
;
;
;(tx-balance txs)
;(def coll-indexed (tx-descendants txs))
;(tx-balance-reducer {} (first coll-indexed))
;(calculate-balance-tx (first coll-indexed))


;(def txs (m-core/extract (chassis.domains.transaction/find-transactions-by-session 1)))
;(chassis.domains.transaction/balance txs)
;
;(t/find-genesis (t/index txs))
;
;(def idx-txs (zipmap (map :id  txs) txs))
;
;(t/save-into-txs-map idx-txs {} 1 (1 idx-txs))
;(chassis.domains.transaction/tx-descendants txs)
;
;(m-core/extract (chassis.services.refund/refund "213" {:id 1} {:amount 10}))

;(def order {"SUBMITTED_FOR_SETTLEMENT" 0
;            "AUTHORIZED" 0
;            "SETTLED" 1})
;
;
;(def txs [{:id 1 :status "SUBMITTED_FOR_SETTLEMENT" :balance 500}
;          {:id 2 :status "SETTLED" :balance 200}
;          {:id 3 :status "AUTHORIZED" :balance 600}])
;
;(defn sort-fn [{status :status balance :balance :as a1}]
;  [(get order status 100) balance])
;
;(sort-by sort-fn (fn [[a1 a2] [b1 b2]]
;                   (println a1 b1 a2 b2)
;                   (if (= a1 b1)
;                     (compare b2 a2)
;                     (compare a1 b1)))  txs)


;(m-core/extract (->
;                  (t/find-transactions-by-session 1)
;                  (m-core/bind t/positive-balance)
;                  (m-core/bind (partial t/eligible-for-refund 200))))

(defn make-session [correlation]
  (m-core/extract (chassis.services.payment-sessions/create {:correlation correlation})))
;
;{ "amount": 501,
;          "currency": "EUR",
; "payment-method": "cc",
; "payment-entity": "ICPT",
; "items": [{
;            "name": "whatever name",
;                 "qty": 1,
;            "total":501,
;            "unit": 501}]}
;


(defn make-order [session amount type]
  (m-core/extract
    (chassis.domains.order/save
      (chassis.domains.order/build session {:type type
                                            :amount amount
                                            :currency "EUR"
                                            :provider "BRAINTREE-TEST"
                                            :payment-entity "ICPT"
                                            :payment-method "cc"
                                            :items [{:name "test-name"
                                                     :qty 1
                                                     :total amount
                                                     :unit amount}]}))))

(defn make-sale [session order]
  (m-core/extract (chassis.services.sale/sale session order)))

;;;
;(def customer-with-card "140426619")
;(def session (assoc (make-session "super-correlation-name") :customer-id customer-with-card))
;(println "SESSION-> " (:id session))
;
;(def order-200 (make-order session 200 "SALE"))
;(def order-refund-400 (make-order session 400 "REFUND"))
;
;
;(def sale-200 (make-sale session order-200))
;sale-200
;
;(def refunds (map m-core/extract (chassis.services.refund/refund session {:amount 400})))
;refunds
;
;(braintree-clj.transaction/to-map (first refunds))

;
;(def txs (m-core/extract (t/find-transactions-by-session 23)))
;txs
;(def positive-balance (m-core/extract (t/positive-balance txs)))
;positive-balance
;
;(def eligible (m-core/extract (t/eligible-for-refund 400 positive-balance)))
;eligible
;;
;;
;
;
;
;(def tx (first eligible))
;(compensation-commands 400 tx)
;
;
;(def eligible (m-core/extract (t/eligible-for-refund 500 positive-balance)))
;eligible
;
;(generate-commands 500 eligible)
;


;
;;; case 1
;(def refund-amount 100)
;(def txs [{:id 1 :type :sale :amount 100 :status :SUBMITTED_FOR_SETTLEMENT :balance 100 :order 1 :refund-balance 100}])
;
;(refund-amount == refund-balance && :SUBMITTED_FOR_SETTLEMENT) == VOID COMMAND
;
;;;case 2
;(def refund-amount 100)
;(def txs [{:id 1 :type :sale :amount 200 :status :SUBMITTED_FOR_SETTLEMENT :balance 200 :order 1 :refund-balance 200}])
;
;(refund-amount < refund-balance && :SUBMITTED_FOR_SETTLEMENT) == VOID + SALE COMMANDS
;[cmd: void tx 1]
;[cmd: sale :amount (refund-balance - refund-amount)]
;
;;;case 3
;(def refund-amount 200)
;(def txs [{:id 1 :type :sale :amount 150 :status :SUBMITTED_FOR_SETTLEMENT :balance 150 :order 1 :refund-balance 150}
;          {:id 2 :type :sale :amount 100 :status :SUBMITTED_FOR_SETTLEMENT :balance 100 :order 1 :refund-balance 100}])
;
;(refund-amount > refund-balance && :SUBMITTED_FOR_SETTLEMENT) == VOID
;(refund-amount < refund-balance && :SUBMITTED_FOR_SETTLEMENT) == VOID + SALE COMMANDS
;
;
;;;case 4
;(def refund-amount 200)
;(def txs [{:id 1 :type :sale :amount 100 :status :SUBMITTED_FOR_SETTLEMENT :balance 100 :order 1 :refund-balance 100}
;          {:id 2 :type :sale :amount 150 :status :SETTLED :balance 150 :order 1 :refund-balance 150}])
;
;(refund-amount > refund-balance && :SUBMITTED_FOR_SETTLEMENT) == VOID
;(refund-amount < refund-balance && :SETTLED) ==  [ REFUND refund-amount]
;
;
;;;case 4
;(def refund-amount 200)
;(def txs [{:id 1 :type :sale :amount 100 :status :SUBMITTED_FOR_SETTLEMENT :balance 100 :order 1 :refund-balance 100}
;          {:id 2 :type :sale :amount 150 :status :SETTLED :balance 150 :order 1 :refund-balance 150}])
;
;(refund-amount > refund-balance && :SUBMITTED_FOR_SETTLEMENT) == VOID
;(refund-amount < refund-balance && :SETTLED) ==  [ REFUND refund-amount]






;(def refund 50 :order 9)
;   {:id 1 :type :sale :amount 100 :status :SUBMITTED_FOR_SETTLEMENT :available-balance 100 :order 1}
;   if (refund - available-balance) > 0)
;         [{:type :void ... }{:type :sale :amount (refund - available-balance)}])

;1. [{:command :void :gateway-id "DSA321" :tx-parent-id 1 :amount 100 :order 9}
;    {:command :sale :amount 50 :order 9}]
;; uma nota de credito e uma nova invoice


;(def refund 120 :order 10)
;if (refund - available-balance) > 0
;   :else [{:type :void :amount 100 }]
;
;if (refund - available-balance) > 0
;      [:type :refund amount 20 order 10 id 2]
;
;1. [{:command :void :gateway-id "DSA321" :tx-parent-id 1 :amount 100 :order 10}
;    {:command :sale :amount 50 :order 9}]
;; uma nota de credito e uma nova invoice

;; f(txs) -> [ commands ]




