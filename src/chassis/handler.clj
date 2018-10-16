(ns chassis.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [metrics.ring.instrument :refer [instrument instrument-by uri-prefix]]
            [chassis.handlers.core :as core]
            [chassis.handlers.middlewares :as mw]))

(def swagger-api
  (api
    {:swagger
     {:ui   "/"
      :spec "/swagger.json"
      :data {:info                {:title       "Microservices chassis using compojure-api 2.0"
                                   :description "Production ready microservices chassis with authentication, instrumentation, configuration "}
             :consumes            ["application/json"]
             :produces            ["application/json"]
             :tags                []
             :securityDefinitions {:api_key {:type "apiKey"
                                             :name "Authorization"
                                             :in   "header"}}}}}
    core/routes))

(def app
  (-> swagger-api
      mw/log-mdc
      mw/uuid
      (instrument-by uri-prefix)))