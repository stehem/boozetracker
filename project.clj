(defproject boozetracker "0.1.0-SNAPSHOT"
            :description "FIXME: write this!"
            :dependencies [[org.clojure/clojure "1.3.0"]
                           [org.clojure/java.jdbc "0.2.3"]
                           [postgresql "9.0-801.jdbc4"]
                           [noir-test2 "1.0.0-SNAPSHOT"]
                           [clj-time "0.4.4"]
                           [noir "1.2.1"]]
            :main boozetracker.server)

