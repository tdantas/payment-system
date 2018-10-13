(ns chassis.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [metrics.ring.instrument :refer [instrument instrument-by uri-prefix]]
            [chassis.handlers.core :as core]))

(def swagger-api
  (api
    {:swagger
     {:ui   "/"
      :spec "/swagger.json"
      :data {:info                {:title       "Microservices chassis using compojure-api 2.0"
                                   :description "Production ready microservices chassis with authentication, instrumentation, configuration "}
             :consumes            ["application/json"]
             :produces            ["application/json"]
             :tags                [{:name "schema", :description "math with schema coercion"}
                                   {:name "spec", :description "math with clojure.spec coercion"}
                                   {:name "data-spec", :description "math with data-specs coercion"}]
             :securityDefinitions {:api_key {:type "apiKey"
                                             :name "Authorization"
                                             :in   "header"}}}}}
    core/routes))

(defn uuid-generator [handler]
  (fn [request]
    (let [params (:params request)
          uuid   (.toString (java.util.UUID/randomUUID))
          params (merge params {:uuid uuid})]
      (handler (merge request {:params params})))))

(def app
  (-> swagger-api
      ;; expose-metrics-as-json ;; are exposed via swagger
      uuid-generator
      (instrument-by uri-prefix)))
