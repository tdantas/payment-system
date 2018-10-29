(ns chassis.domains.transaction
  (:require [clojure.core :as clj]
            [cats.core :refer [foldl return]]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [chassis.failure :refer [wrap-try failure exception]]
            [cats.monad.either :refer [left right]]
            [chassis.repositories.transactions :as rtx]))


(def status-order {"AUTHORIZED" 0
                   "SUBMITTED_FOR_SETTLEMENT" 0
                   "SETTLED" 2})

(defrecord Transaction [id order-id amount status response created-at gateway-id uuid type final-state provider tx-parent-id])

(defn build [{:keys [order-id uuid status amount response gateway-id final-state type tx-parent-id provider]}]
  (map->Transaction {:order-id order-id
                     :uuid uuid
                     :type type
                     :status status
                     :response response
                     :amount amount
                     :final-state final-state
                     :tx-parent-id tx-parent-id
                     :provider provider
                     :gateway-id gateway-id}))

(defn- snake->Transaction [db-order]
  (map->Transaction (transform-keys ->kebab-case-keyword db-order)))

(defn find-transactions-by-session [session-id]
  (right (map snake->Transaction (rtx/find-by-session session-id))))

(defn save [^Transaction transaction]
  (wrap-try "transaction.save.failed"
            (snake->Transaction (rtx/insert transaction))))

(defn find-genesis [txs-map id]
  (loop [id-tx id]
    (let [tx (get txs-map id-tx)
          pid (:tx-parent-id tx)]
      (if (nil? pid)
        (:id tx)
        (recur pid)))))

(defn save-into-txs-map [index acc key value]
  (let [genesis (find-genesis index key)
        tx  (get index genesis)
        tx-descendants (:descendants tx [])]
    (if (= key genesis)
      (assoc acc genesis tx)
      (assoc acc genesis
                 (assoc tx :descendants
                           (conj (get-in acc [genesis :descendants] []) value))))))

(defn index [txs]
  (zipmap (map :id  txs) txs))

(defn build-descendants [txs]
  (let [idx-tx (index txs)]
    (vals (reduce-kv(partial save-into-txs-map idx-tx) {} idx-tx))))


(defn real-amount [tx]
  (let [amount (:amount tx)]
    (case (:type tx)
      "SALE"    amount
      "REFUND"  (- amount)
      "DEPOSIT" 0
      "VOID"    (- amount)
      (- amount))))


(defn calculate-balance [tx]
  (let [tx-descendants (:descendants tx)
        tx-balance (real-amount tx)
        tx-descendants-balance (map real-amount tx-descendants)]
    (apply + (conj tx-descendants-balance tx-balance))))


(defn balance-reducer [acc tx]
  (conj acc (assoc tx :available-balance (calculate-balance tx))))

(defn balance [txs]
  (reduce  balance-reducer [] (build-descendants txs)))


(defn positive-balance [txs]
  (right (filter #(> (:available-balance %) 0) (balance txs))))


(defn sort-fn [{status :status balance :available-balance}]
  [(get status-order status 100) balance])

(defn sort-by-status [txs]
  (sort-by sort-fn (fn [[a1 a2] [b1 b2]]
                     (if (= a1 b1)
                       (compare b2 a2)
                       (compare a1 b1)))  txs))

(defn eligible-for-refund [amount coll-txs]
 (loop [amount amount
        [h & t] (sort-by-status coll-txs)
         result []]
   (let [balance (:available-balance h)]
      (cond
        (nil? h) (left (failure "transactions.eligibles.not-found.refund"))
        (pos? (- amount balance)) (recur (- amount balance) t (conj result (assoc h :refund-balance balance)))
        :else (right (conj result (assoc h :refund-balance amount)))))))

(defmulti compensation-commands (fn [amount {status :status
                                             available-balance :available-balance
                                             refund-balance :refund-balance
                                             :as tx}]
                                  (let [status (keyword status)
                                        balance (- amount available-balance)]
                                    (cond
                                      (= refund-balance available-balance)  [status :total]
                                       :else                                [status :partial]))))

(defmethod compensation-commands [:AUTHORIZED :total] [amount tx]
  [{:command :void :tx tx :amount (:refund-balance tx)}])

(defmethod compensation-commands [:SUBMITTED_FOR_SETTLEMENT :total] [amount tx]
  [{:command :void :tx tx :amount (:refund-balance tx)}])

(defmethod compensation-commands [:AUTHORIZED :partial] [amount tx]
  [{:command :void :tx tx :amount (:amount tx)}
   {:command :sale :tx tx :amount (- (:available-balance tx) (:refund-balance tx))}])

(defmethod compensation-commands [:SUBMITTED_FOR_SETTLEMENT :partial] [amount tx]
  [{:command :void :tx tx :amount (:amount tx)}
   {:command :sale :tx tx :amount (- (:available-balance tx) (:refund-balance tx))}])

(defmethod compensation-commands [:SETTLED :total] [amount tx]
  [{:command :refund :tx tx :amount (:refund-balance tx)}])

(defmethod compensation-commands [:SETTLED :partial] [amount tx]
  [{:command :refund :tx tx :amount (:refund-balance tx)}])

(defmethod compensation-commands :default [amount tx]
  (throw (Exception. "METHOD DISPATCH ERROR COMPENSATION COMMANDS REFUND")))

(defn command-reducer [{amount :amount
                        commands :commands :as acc} tx]
  (let [refund-balance (:refund-balance tx)
        commands       (concat commands (compensation-commands amount tx))
        new-amount     (- refund-balance amount)]
    (assoc acc :commands commands :amount new-amount)))

(defn generate-commands [refund-amount eligible-txs]
  (println eligible-txs)
  (right (reduce command-reducer {:amount refund-amount :commands []} eligible-txs)))

















