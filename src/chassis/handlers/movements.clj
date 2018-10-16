(ns chassis.handlers.movements
  (:require [compojure.api.sweet :refer [context GET POST resource]]
            [ring.util.http-response :refer :all]
            [clojure.spec.alpha :as s]
            [spec-tools.spec :as spec]
            [spec-tools.data-spec :as dspec]

            [chassis.http :as http]

            [chassis.services.movements :as movements]
            [metrics.ring.expose :refer [render-metrics serve-metrics]]
            [chassis.repositories.payment-sessions :as ps]
            [chassis.handlers.middlewares :as mw]))


(defn get-movements [uuid payment-session]
  (http/translate (movements/find-by-session payment-session)))

(def app
  (context "/payment-sessions/:psid" []
    :tags ["spec"]
    :coercion :spec
    :path-params [psid :- spec/int?]
    :middleware [mw/load-payment-session]

    (GET "/movements" {{:keys [uuid session]} :app :as r}
      (get-movements uuid session))))

