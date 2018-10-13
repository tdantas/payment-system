(ns chassis.braintree
  (:require [chassis.config :refer [config]]
            [mount.core :refer [defstate]]
            [braintree-clj.core :as bt-core]
            [braintree-clj.gateway.customer :as bt-cg]
            [braintree-clj.customer-request :as bt-cr]
            [braintree-clj.customer :as bt-customer]
            [braintree-clj.result :as result]))

(defn build-gateway [conf]
   (cond
     (nil? (:braintree conf)) (bt-core/create-gateway)
     :else (bt-core/create-gateway (:braintree conf))))

(defstate gateway
          :start (build-gateway config))

(defn customer-request [{:keys [email first-name last-name :as params]}]
  (bt-cr/create params))

(defn customer-create
  ([params] (customer-create gateway params))
  ([gateway cr-params]
   (let [customer-result (bt-cg/create gateway (customer-request cr-params))
         customer (result/target customer-result)]
        (if (result/success? customer-result)
          (bt-customer/to-map customer)
          (throw (ex-info "cant create customer" {:errors (result/errors customer-result)}))))))