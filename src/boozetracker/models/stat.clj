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

(def custom-formatter (formatter "dd-MM-YYYY"))
(def custom-formatter-r (formatter "MM-YYYY"))


(defn to-cljdate
  [d]
  (parse custom-formatter d))


(def week {1 "Monday" 2 "Tuesday" 3 "Wednesday" 4 "Thursday" 5 "Friday" 6 "Saturday" 7 "Sunday"})


(defn to-epoch
  [date]
  (to-long (to-cljdate date)))


(defn for-current-user
  []
  (select costs
    (where {:user_id (:id (User/current-user))}))) 

(defn has-costs?
  []
  (not (empty? (for-current-user))))

(defn grouped-by
  [f]
  (let [grouped f] 
    (for [[type costs] grouped] (into [] [type (reduce + (map #(Float/parseFloat (:cost %)) costs))]))))


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
    (fields (raw "to_char(date, 'DDD') AS date, cost")) 
    (where {:user_id (:id (User/current-user))})
    (order (raw "date") :ASC)))


(defn average-cost-grouped-by-date
  []
  (select costs
    (fields (raw "date_trunc('month', date) AS month, SUM(cost), to_char(date, 'Mon') || to_char(date, 'YY') AS time")) 
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
  (chart (costs-grouped-by-date) :date))


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

  


;TODO gauge alcoholism level 


(defn gauge
  []
  (let [spend (avg-spend-month)]
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
