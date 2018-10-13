(ns chassis.auth.token
  (:require [compojure.api.sweet :refer :all]
            [compojure.api.meta :refer [restructure-param]]
            [ring.util.http-response :refer :all]
            [chassis.config :refer [config]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.auth.backends :as backends]
            [buddy.auth.accessrules :refer [wrap-access-rules]]
            [buddy.auth :refer [authenticated?]]))

(defn token-authfn [_ token]
  (let [token (keyword token)]
    (get-in config [:api_tokens token])))

(def backend
  (backends/token {:authfn token-authfn}))

(defn access-error [_ _]
  {:status 403 :headers {} :body "Unauthorized"})

(defn wrap-rule [handler rule]
  (-> handler
      (wrap-access-rules {:rules [{:pattern #".*"
                                   :handler rule}]
                          :on-error access-error})))

(defmethod restructure-param :auth-rules
  [_ rule acc]
  (update-in acc [:middleware] conj [wrap-rule rule]))


