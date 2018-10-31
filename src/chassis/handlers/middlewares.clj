(ns chassis.handlers.middlewares
  (:require [clojure.core.match :as pattern]
            [chassis.repositories.request-log :as request-log]
            [ring.util.http-response :refer :all]
            [clojure.tools.logging :as log]
            [chassis.domains.session :as session]
            [ring.util.response :as resp]
            [clojure.string :as s])
  (:import (org.slf4j MDC)))

(defn load-payment-session [handler]
  (fn [{:keys [params] :as request}]
    (pattern/match (session/find-by-id (:psid params))
       {:right s}  (handler (merge-with merge request {:app {:session s}}))
       {:left f}   (not-found f))))


(defn register-request-log [request-type {method :request-method
                                          url    :uri
                                          path   :route-params
                                          body   :body-params
                                          query  :query-params
                                          {uuid :uuid} :app :as request} response]
  (request-log/insert {:uuid uuid
                       :type request-type
                       :method (s/upper-case (name method))
                       :url url
                       :status (:status response)
                       :response response
                       :payload (merge {} {:body body} {:query query} {:path path})}))

(defn request-log [type]
  (fn [handler]
    (fn [request]
        (try
           (log/info "registering request log")
           (let [response (handler request)]
             (future (register-request-log type request response))
             response)
          (catch Exception e
            (do
              (future (register-request-log type request {:status 500 :response { :msg (.getMessage e)}}))
              (throw e)))))))

(defn uuid [handler]
  (fn [request]
    (let [uuid (.toString (java.util.UUID/randomUUID))]
      (some-> (handler (merge request {:app  {:uuid uuid}}))
              (resp/header "x-request-id" uuid)))))

(defn log-mdc [handler]
  (fn [request]
    (try
      (when-let [uuid (get-in request [:app :uuid])]
        (MDC/put "uuid" uuid))
      (handler request)
      (finally
        (MDC/clear)))))