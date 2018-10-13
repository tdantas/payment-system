(ns chassis.handlers.core
  (:require [compojure.api.sweet :refer [context]]
            [chassis.handlers.payment-sessions :as payment-sessions]
            [chassis.handlers.audits :as audits]
            [chassis.handlers.transactions :as transactions]))



(def routes
  (context "/api" []
    :tags ["spec"]
    :coercion :spec

    payment-sessions/app
    audits/app
    transactions/app))
