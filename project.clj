(defproject lds-scriptures-api "0.1.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :aot :all
  :description "Fancy scripture api in clojure"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.slf4j/slf4j-log4j12 "1.7.2"]
                 [noir "1.3.0-beta3"]
                 [korma "0.3.0-RC5"]

                 [enlive "1.1.5"]

                 [clj-bonecp-url "0.1.0"]

                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [org.postgresql/postgresql "9.2-1003-jdbc3"]

                 [compojure "1.1.6"]
                 [ring-server "0.3.1"]
                 [ring/ring-json "0.2.0"]
                 [ring/ring-jetty-adapter "1.2.0"]]

  :plugins [[lein-ring "0.8.10"]
            [lein-beanstalk "0.2.7"]]
  :ring {:handler lds-scriptures-api.handler/app}
  :profiles

  ;; Development
  {:dev {:dependencies [[ring-mock "0.1.5"]
                        [ring/ring-devel "1.2.1"]]}})
