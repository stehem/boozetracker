(ns boozetracker.db
 (:use [somnium.congomongo]))


(def conn
  (make-connection "beertabs"
                   :host "127.0.0.1"
                   :port 27017))

