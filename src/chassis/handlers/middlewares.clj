(ns chassis.handlers.middlewares
  (:require [chassis.services.payment-sessions :as ps]
            [clojure.core.match :as pattern]
            [chassis.repositories.request-log :as request-log]
            [ring.util.http-response :refer :all]
            [clojure.tools.logging :as log]
            [chassis.domains.session :as session]
            [clojure.string :as s])
  (:import (org.slf4j MDC)))

(defn load-payment-session [handler]
  (fn [{:keys [params] :as request}]
    (pattern/match (session/find-by-id (:psid params))
       {:right s}  (handler (merge-with merge request {:app {:session s}}))
       {:left f}   (not-found f))))

(defn register-intent [intent-type]
  (fn [handler]
    (fn [{method :request-method
          url    :uri
          path   :route-params
          body   :body-params
          query  :query-params
          {uuid :uuid} :app :as request}]
        (try
           (log/info "registering request intent" method url)
           (future (request-log/insert {:uuid uuid
                                        :type intent-type
                                        :method (s/upper-case (name method))
                                        :url url
                                        :payload (merge {} {:body body} {:query query} {:path path})}))
          (catch Exception e (log/error "request log failed to save" method url e)))
        (handler request))))

(defn uuid [handler]
  (fn [request]
    (let [uuid (.toString (java.util.UUID/randomUUID))]
      (handler (merge request {:app  {:uuid uuid}})))))

(defn log-mdc [handler]
  (fn [request]
    (try
      (when-let [uuid (get-in request [:app :uuid])]
        (MDC/put "uuid" uuid))
      (handler request)
      (finally
        (MDC/clear)))))