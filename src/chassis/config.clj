(ns chassis.config
  (:require [omniconf.core :as cfg]
            [mount.core :refer [defstate]]))

(defn init [cli-args]
  (cfg/define
    {:help {:description      "prints this help message"
            :help-name        "main app"
            :help-description "available settings. see https://github.com/grammarly/omniconf#configuration-scheme-syntax for more info"}

     :env        {:description "system environment"
                  :type :string
                  :default "development"}

     :api_tokens {:description "tokens that should be sent on the 'Authorization: token xxxx' header"
                  :type        :edn
                  :default     '{:11111111111111 :admin}}

     :server_port {:description "port where the server will run"
                   :type        :number
                   :default     3000}

     :database_url {:description "jdbc database url"
                    :type        :string
                    :required    true
                    :default     "jdbc:postgresql://localhost:5432/payment_system_dev"}

     :braintree {:nested { :merchant-id {:type :string :default "rqkh7jrypryrn8gy"}
                           :private-key {:type :string :default "b85de79aaf466933c6266369f63fa290"}
                           :public-key  {:type :string :default "drb8xdzsq9qzntxk"}
                           :environment {:type :string :default "sandbox"}}}})




  (cfg/populate-from-cmd cli-args)
  (when-let [conf-file (cfg/get :conf-file)]
    (cfg/populate-from-file conf-file))
  (cfg/populate-from-env)
  (cfg/verify))


(defstate config 
  :start (do
           (init (mount.core/args))
           (cfg/get))) ;;return all opts
