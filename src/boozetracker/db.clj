(ns boozetracker.db
(:require [clojure.java.jdbc :as sql]))

(defn fetch
  [query]
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/with-query-results results
      query
      (into [] results))))

(defn insert
  [table data]
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/insert-record table data)))


(defn update
  [table where data]
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/update-values table where data)))


(defn delete
  [table where]
  (sql/with-connection (System/getenv "DATABASE_URL")
    (sql/delete-rows table where)))
;export DATABASE_URL=postgresql://postgres:jajapostgres@localhost:5432/boozetracker
;export DATABASE_URL=postgresql://postgres:jajapostgres@localhost:5432/boozetracker-test
