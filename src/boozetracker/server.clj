(ns boozetracker.server
  (:require [noir.server :as server]))

(server/load-views "src/boozetracker/")

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "9090"))]
    (server/start port {:mode mode
                        :ns 'boozetracker})))

