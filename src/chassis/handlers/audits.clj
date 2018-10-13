(ns chassis.handlers.audits
  (:require [compojure.api.sweet :refer [routes context GET POST resource]]
            [ring.util.http-response :refer [ok]]
            [spec-tools.spec :as spec]

            [metrics.ring.expose :refer [render-metrics serve-metrics]]
            [chassis.repositories.payment-sessions :as ps]
            [chassis.repositories.audits :as ra]))

(def app
  (routes
    (GET "/audits/:table-name/:id" []
      :path-params [table-name :- spec/string?
                    id :- spec/int?]

      (ra/fetch-by table-name ["id" "=" (str id)]))))