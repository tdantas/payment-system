(defproject chassis "0.1.0-SNAPSHOT"
  :description "Compojure-api 2.0.0 alpha microservices chassis"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]

                 [ring/ring-core "1.6.3"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [mount "0.1.13"]
                 [com.grammarly/omniconf "0.3.2"]
                 [robert/hooke "1.3.0"]
                 
                 ;; SQL                                  
                 [org.postgresql/postgresql "42.2.5.jre7"]
                 [hikari-cp "2.6.0"]
                 [migratus "1.0.9"]
                 [org.clojure/java.jdbc "0.7.8"]
                 [com.layerware/hugsql "0.4.9"]
                 [postgre-types "0.0.4"]

                 [metosin/compojure-api "2.0.0-alpha21"]
                 [metosin/spec-tools "0.7.1"]
                 [manifold "0.1.6"]

                 ;; these are on local_repo
                 [buddy/buddy-auth "2.1.0"] ;; waiting release version > 2.1, async support is merged
                 [metrics-clojure "3.0.0-SNAPSHOT"] ;; waiting release >= 3, async support is merged
                 [metrics-clojure-ring "3.0.0-SNAPSHOT"] ;; waiting release >= 3, async support is merged

                 [org.clojure/tools.logging "0.4.1"]
                 [ch.qos.logback/logback-classic "1.2.3"]

                 [potemkin "0.4.5"]

                 [org.slf4j/jul-to-slf4j "1.7.25"]

                 ;;json
                 [cheshire "5.8.1"]
                 [camel-snake-kebab "0.4.0"]


                 [metosin/muuntaja-cheshire "0.6.1"]

                 ;; monads
                 [funcool/cats "2.2.0"]
                 [org.clojure/core.match "0.3.0-alpha5"]

                 [org.clojars.tdantas/braintree-clj "0.1.1"]]


  :repositories {"local" ~(str (.toURI (java.io.File. "local_repo")))}
  :checksum :ignore

  :aot [clojure.tools.logging.impl chassis.main]
  :main chassis.main
  :repl-options {:init-ns user
                 :caught clj-stacktrace.repl/pst+}
  
  ;; > lein ring server
  ;; doesn't do validation, preferrable to call 'lein run'
  :ring {:handler chassis.handler/app
         ;; :init    chassis.main/init
         :async?  true
         :nrepl   {:start? true}}

  :aliases {"verify"     ["run" "-m" "chassis.main/verify"] 
            "migrations" ["run" "-m" "chassis.db/migrations"]}

  :uberjar-name "server.jar"
  :profiles {:uberjar {:aot :all 
                       :main chassis.main}
                       
             :dev     {:source-paths   ["dev"]
                       :resource-paths ["resources"]
                       :dependencies   [[ring/ring-devel "1.6.3"]
                                        [ring/ring-mock "0.3.2"]
                                        [eftest "0.5.2"]
                                        [clj-stacktrace "0.2.8"]]
                       :plugins        [[lein-ring "0.12.3"]
                                        [lein-eftest "0.5.2"]]}})
