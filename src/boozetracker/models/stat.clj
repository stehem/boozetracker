(ns boozetracker.models.stat
  (:require [boozetracker.db :as db]
            [clojure.string]
            [boozetracker.models.user :as User])
  (:use [clj-time.core :exclude [extend]]
        [clj-time.format]
        [clj-time.coerce]
        [boozetracker.orm]
        ))

(use 'korma.core)


(defn for-current-user
  []
  (select costs
    (where {:user_id (:id (User/current-user))}))) 

(defn has-costs?
  []
  (not (empty? (for-current-user))))

(defn costs-grouped-by-type
  []
  (select costs
    (fields :type (raw "SUM(cost)")) 
    (where {:user_id (:id (User/current-user))}) 
    (group :type)))
  
  
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
  (select costs
    (fields (raw "to_char(date, 'Dy') AS day, SUM(cost)")) 
    (where {:user_id (:id (User/current-user))}) 
    (group (raw "day"))))


(defn costs-grouped-by-month
  []
  (select costs
    (fields (raw "to_char(date, 'Mon') || to_char(date, 'YY') AS time, SUM(cost)")) 
    (where {:user_id (:id (User/current-user))}) 
    (group (raw "time"))))
  

(defn costs-grouped-by-date
  []
  (select costs
    (fields (raw "to_char(date, 'DDD') AS date, to_char(date, 'DD-MM-YY') AS time, cost")) 
    (where {:user_id (:id (User/current-user))})
    (order (raw "date") :ASC)))


(defn average-cost-grouped-by-date
  []
  (select costs
    (fields (raw "date_trunc('month', date) AS month, SUM(cost), to_char(date, 'DD-MM-YY') AS time")) 
    (where {:user_id (:id (User/current-user))})
    (where (raw "cost IS NOT NULL"))
    (group (raw "month, time"))
    (order (raw "month") :ASC)))

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
  (select costs
    (fields (raw "SUM(cost)"))
    (where {:user_id (:id (User/current-user))})))

(defn total-spend
  []
  (:sum (first (total-spend-query))))


(defn average-spend-by-month-query
  ;WTF with Korma subselects?
  []
  (format 
    "SELECT AVG(sum)
    FROM
    (
      SELECT date_trunc('month', date) AS month, SUM(cost)
      FROM costs
      WHERE cost IS NOT NULL
      AND user_id = %s
      GROUP BY date
    ) AS sub " (User/current-user-id)))
  

(defn average-spend-by-month
  []
  (format "%.2f" (:avg (first (exec-raw [(average-spend-by-month-query)] :results)))))


(defn number-of-days-between-now-and-first-query
  []
  (select costs
    (fields (raw "DATE_PART('days', NOW() - date) AS days"))
    (where {:user_id (:id (User/current-user))})
    (group (raw "date"))
    (order (raw "date") :ASC)
    (limit 1)))

(defn number-of-days-between-now-and-first
  []
  (:days (first (number-of-days-between-now-and-first-query))))

(defn average-spend-by-day
  []
  (format "%.2f" (/ (total-spend) (number-of-days-between-now-and-first))))

(defn number-of-sessions-query
  []
  (select costs
    (fields (raw "COUNT(*)"))
    (where {:user_id (:id (User/current-user))})))

(defn number-of-sessions
  []
  (:count (first (number-of-sessions-query))))
          

(defn average-spend-by-session
  []
  (format "%.2f" (/ (total-spend) (number-of-sessions))))

(defn average-number-of-drinks-query
  []
  (select costs
    (fields (raw "AVG(unit) AS avg"))
    (where {:user_id (:id (User/current-user))})))


(defn average-number-of-drinks
  []
  (format "%.2f" (:avg (first (average-number-of-drinks-query)))))

(defn average-drink-price-query
  []
  (format 
    "SELECT AVG(avg_cost_per_unit) AS price
    FROM
    (
      SELECT (cost/unit) AS avg_cost_per_unit
      FROM costs
      WHERE user_id = %s
    ) AS sub " (User/current-user-id)))

(defn average-drink-price
  []
  (format "%.2f" (:price (first (exec-raw [(average-drink-price-query)] :results)))))
  

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
