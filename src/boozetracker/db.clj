(ns boozetracker.db
 (:use [somnium.congomongo]))


(defn split-mongo-url [url]
  "Parses mongodb url from heroku, eg. mongodb://user:pass@localhost:1234/db"
  (let [matcher (re-matcher #"^.*://(.*?):(.*?)@(.*?):(\d+)/(.*)$" url)] ;; Setup the regex.
    (when (.find matcher) ;; Check if it matches.
      (zipmap [:match :user :pass :host :port :db] (re-groups matcher))))) ;; Construct an options map.


(def conn
  (let [mongo-url (get (System/getenv) "MONGOHQ_URL")]
    (if mongo-url
      (let [config (split-mongo-url mongo-url)]
        (authenticate
          (make-connection  (:db config)
                            (:host config)
                            (Integer. (:port config)))
          (:user config) (:pass config)))
        (make-connection "beertabs"
                          :host "127.0.0.1"
                          :port 27017) )))


