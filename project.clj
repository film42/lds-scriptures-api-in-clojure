(defproject lds-scriptures-api "0.1.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :description "Fancy scripture api in clojure"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [noir "1.3.0-beta3"]
                 [korma "0.3.0-RC5"]
                 [org.clojure/java.jdbc "0.3.0-alpha5"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 [compojure "1.1.5"]
                 [ring/ring-json "0.2.0"]
                 [ring/ring-jetty-adapter "1.2.0"]]
  :main lds-scriptures-api.core

  :plugins [[lein-ring "0.8.5"]]
  :ring {:handler lds-scriptures-api.handler/app}
  :profiles

  ;; Development
  {:dev {:dependencies [[ring-mock "0.1.5"]]}})
