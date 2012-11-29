(ns boozetracker.db
 (:use [somnium.congomongo]))


(defn split-mongo-url [url]
  "Parses mongodb url from heroku, eg. mongodb://user:pass@localhost:1234/db"
  (let [matcher (re-matcher #"^.*://(.*?):(.*?)@(.*?):(\d+)/(.*)$" url)] ;; Setup the regex.
    (when (.find matcher) ;; Check if it matches.
      (zipmap [:match :user :pass :host :port :db] (re-groups matcher))))) ;; Construct an options map.

(println "LOLOLOLOLO")

(def conn
        (make-connection (get (System/getenv) "MONGOHQ_URL")) )


