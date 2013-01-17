(ns boozetracker.models.stat
  (:require [boozetracker.db :as db]
            [clojure.string]
            [boozetracker.models.user :as User])
  (:use [clj-time.core :exclude [extend]]
        [clj-time.format]
        [clj-time.coerce]
        ))



(defn for-current-user
  []
  (db/fetch
    ["SELECT *
     FROM costs
     WHERE user_id = ?" (User/current-user-id)]))


(defn has-costs?
  []
  (not (empty? (for-current-user))))

(defn costs-grouped-by-type
  []
  (db/fetch
    ["SELECT SUM(cost), type
     FROM costs
     WHERE user_id = ?
     GROUP BY type" (User/current-user-id)]))

  
(defn to-js
  [name value]
  (format "['%s', %s]" name value))

(defn join-with-commas
  [seq]
  (apply str (interpose \, seq)))

(defn chart-raw
  [query col]
  (map (fn[x] (to-js (col x) (or (:sum x) (:cost x))  )) query))

(defn chart
  [query col]
  (join-with-commas (chart-raw query col)))

(defn chart-type
  []
  (chart (costs-grouped-by-type) :type))


(defn costs-grouped-by-day
  []
  (db/fetch
    ["SELECT to_char(date, 'Dy') AS day, SUM(cost)
     FROM costs
     WHERE user_id = ?
     GROUP BY day" (User/current-user-id)]))


(defn costs-grouped-by-month
  []
  (db/fetch
    ["SELECT to_char(date, 'Mon') || to_char(date, 'YY') AS time, SUM(cost)
     FROM costs
     WHERE user_id = ?
     GROUP BY time" (User/current-user-id)]))
  

(defn costs-grouped-by-date
  []
  (db/fetch
    ["SELECT to_char(date, 'DDD') AS date, to_char(date, 'DD-MM-YY') AS time, cost
     FROM costs
     WHERE user_id = ?
     ORDER BY date ASC" (User/current-user-id)]))


(defn average-cost-grouped-by-date
  []
  (db/fetch
    ["SELECT date_trunc('month', date) AS month, SUM(cost), to_char(date, 'DD-MM-YY') AS time
     FROM costs
     WHERE user_id = ?
     GROUP BY month, time
     ORDER BY month ASC" (User/current-user-id)]))



(defn pie-chart-day
  []
  (chart (costs-grouped-by-day) :day))


(defn column-chart-month
  []
  (chart (costs-grouped-by-month) :time))


(defn line-chart-date
  []
  (chart (costs-grouped-by-date) :time))


(defn line-chart-average-costs
  []
  (chart (average-cost-grouped-by-date) :time))


(defn total-spend-query
  []
  (db/fetch
    ["SELECT SUM(cost)
     FROM costs
     WHERE user_id = ?" (User/current-user-id)]))


(defn total-spend
  []
  (:sum (first (total-spend-query))))


(defn average-spend-by-month-query
  []
  (db/fetch
    ["SELECT AVG(sum)
      FROM
      (
        SELECT date_trunc('month', date) AS month, SUM(cost)
        FROM costs
        WHERE cost IS NOT NULL
        AND user_id = ?
        GROUP BY date
      ) AS sub" (User/current-user-id)]))
  

(defn average-spend-by-month
  []
  (format "%.2f" (:avg (first (average-spend-by-month-query)))))


(defn number-of-days-between-now-and-first-query
  []
  (db/fetch
    ["SELECT DATE_PART('days', NOW() - date) AS days
     FROM costs
     WHERE user_id = ?
     GROUP BY date
     ORDER BY date ASC
     LIMIT 1" (User/current-user-id)]))


(defn ^:dynamic number-of-days-between-now-and-first
  []
  (let [result (:days (first (number-of-days-between-now-and-first-query)))]
    (if (= 0.0 result)
      1
      result)))

(defn average-spend-by-day
  []
  (format "%.2f" (/ (total-spend) (number-of-days-between-now-and-first))))

(defn number-of-sessions-query
  []
  (db/fetch
    ["SELECT COUNT(*)
     FROM costs
     WHERE user_id = ?" (User/current-user-id)]))

(defn number-of-sessions
  []
  (:count (first (number-of-sessions-query))))
          

(defn average-spend-by-session
  []
  (format "%.2f" (/ (total-spend) (number-of-sessions))))

(defn average-number-of-drinks-query
  []
  (db/fetch
    ["SELECT AVG(unit) AS avg
     FROM costs
     WHERE user_id = ?" (User/current-user-id)]))


(defn average-number-of-drinks
  []
  (format "%.2f" (:avg (first (average-number-of-drinks-query)))))

(defn average-drink-price-query
  []
  (db/fetch
    ["SELECT AVG(avg_cost_per_unit) AS price
      FROM
      (
        SELECT (cost/unit) AS avg_cost_per_unit
        FROM costs
        WHERE user_id = ?
      ) AS sub" (User/current-user-id)]))

(defn average-drink-price
  []
  (format "%.2f" (:price (first (average-drink-price-query)))))
  

;TODO gauge alcoholism level 


(defn gauge
  []
  (let [spend (Float/parseFloat (average-spend-by-month))]
    (cond
      (and (> spend 0) (< spend 20)) "20"
      (and (> spend 20) (< spend 40)) "30"
      (and (> spend 40) (< spend 60)) "35"
      (and (> spend 60) (< spend 80)) "40"
      (and (> spend 80) (< spend 100)) "50"
      (and (> spend 100) (< spend 150)) "55"
      (and (> spend 150) (< spend 200)) "60"
      (and (> spend 200) (< spend 250)) "70"
      (and (> spend 250) (< spend 300)) "80"
      (> spend 300) "90"  )))




; ONLY ONE COST MAX PER DATE
