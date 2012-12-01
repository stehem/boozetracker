(ns boozetracker.db
  (:use [somnium.congomongo])
  (:use [somnium.congomongo.config :only [*mongo-config*]]))

(defn split-mongo-url [url]
  "Parses mongodb url from heroku, eg. mongodb://user:pass@localhost:1234/db"
  (let [matcher (re-matcher #"^.*://(.*?):(.*?)@(.*?):(\d+)/(.*)$" url)] ;; Setup the regex.
    (when (.find matcher) ;; Check if it matches.
      (zipmap [:match :user :pass :host :port :db] (re-groups matcher))))) ;; Construct an options map.

(defn conn []
"Checks if connection and collection exist, otherwise initialize."
    (let [mongo-url (get (System/getenv) "MONGOHQ_URL")] ;; Heroku puts it here.
      (if mongo-url
    (let [config (split-mongo-url mongo-url)] ;; Extract options.
      (set-connection! (make-connection :db (:db config) :host (:host config) :port (Integer. (:port config))))
      )
      (set-connection! (make-connection "beertabs" "127.0.0.1" 27017))
    )
  )
  )
