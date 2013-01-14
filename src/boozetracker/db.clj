(ns boozetracker.db)

(use 'korma.db)

(def db-url
  (or (System/getenv "DATABASE_URL") "postgres://postgres:jajapostgres@localhost:5432/boozetracker"))

(defn split-db-url [url]
  "Parses database url from heroku, eg. postgres://user:pass@localhost:1234/db"
  (let [matcher (re-matcher #"^.*://(.*?):(.*?)@(.*?):(\d+)/(.*)$" url)] ;; Setup the regex.
    (when (.find matcher) ;; Check if it matches.
      (zipmap [:match :user :password :host :port :db] (re-groups matcher))))) ;; Construct an options map.


(defdb dev (postgres (split-db-url db-url)))



