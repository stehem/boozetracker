(ns boozetracker.db
  (:use [somnium.congomongo])
  (:use [somnium.congomongo.config :only [*mongo-config*]]))

(defn split-mongo-url [url]
  (let [matcher (re-matcher #"^.*://(.*?):(.*?)@(.*?):(\d+)/(.*)$" url)]
    (when (.find matcher)
      (zipmap [:match :user :pass :host :port :db] (re-groups matcher)))))

(defn ^:dynamic conn []
  (let [mongo-url (get (System/getenv) "MONGOHQ_URL")]
    (if mongo-url
      (let [config (split-mongo-url mongo-url)]
        (mongo! :db (:db config) :host (:host config) :port (Integer. (:port config)))
        (authenticate (:user config) (:pass config))  )
      (set-connection! (make-connection "beertabs" "127.0.0.1" 27017))  ) ) )
