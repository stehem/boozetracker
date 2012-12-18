(ns boozetracker.models.stat
  (:require [boozetracker.db :as db]
            [clojure.string]
            [boozetracker.models.user :as User])
  (:use [clj-time.core :exclude [extend]]
        [clj-time.format]
        [clj-time.coerce]
        [somnium.congomongo]))


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
  (db/conn)
  (into [] (sort-by :epoch
    (:costs (fetch-one :users :where {:_id (:_id (User/current-user))}))  )))

(defn has-costs?
  []
  (not (empty? (for-current-user))))

(defn grouped-by
  [f]
  (let [grouped f] 
    (for [[type costs] grouped] (into [] [type (reduce + (map #(Float/parseFloat (:cost %)) costs))]))))



  
  

(defn pie-chart
  []
  (grouped-by (group-by :type (for-current-user)  )))


(defn days-chart
  []
  (let [grouped
    (group-by #(first %)
      (map (fn[d] 
             (let [[date cost] d] 
               [(get week (day-of-week (to-cljdate date)) ) cost]))
        (grouped-by (group-by :date (for-current-user)))  ))
        ]
    (into [] (for [[k v] grouped] [k (reduce + (map #(last %) v))]))  ))


(defn total-spend
  []
  (reduce + (reduce (fn[xs x] (conj xs (Float/parseFloat (:cost x)))) [] (for-current-user))))


(defn format-chart
  [pie]
  (format "[%s]" (clojure.string/join ", " (map (fn[p] (format "['%s', %s]" (first p) (second p))) pie))))
    
                       

(defn spend-month
  []
  (grouped-by (group-by 
                #(let [date (to-cljdate (:date %))] (format "%s-%s" (month date) (year date))) 
                (for-current-user)  )))

  
(defn spend-day
  []
  (grouped-by (group-by :date (for-current-user)  )))


(defn avg-spend-month
  []
  (int (/ (total-spend) (count (spend-month)))) )


(defn avg-spend-session
  []
  (int (/ (total-spend) (count (spend-day)))) )


(defn sorted
  [f frame]
  (map #(into [] (butlast %))
    (sort-by last (map #(conj % (to-long (f %))) frame)))  )
 

(defn sorted-spend-month
  []
  (sorted #(parse custom-formatter-r (first %)) (spend-month)))


(defn sorted-spend-day
  []
  (sorted #(to-cljdate (first %)) (spend-day)))


(defn min-date
  []
  (to-cljdate (ffirst (sorted-spend-day))))

  
(defn max-date
  []
  (to-cljdate (first (last (sorted-spend-day)))))


(defn avg-spend-day
  []
  (let [days (in-days (interval (min-date) (max-date))) spend (total-spend)]
    (int (/ spend (if (= 0 days) 1 days)))  ))


(defn total-drinks
  []
  (reduce + (map #(Integer/parseInt (:unit %)) (for-current-user))))


(defn avg-drinks-nb
  []
  (int (/ (total-drinks) (count (for-current-user)))) )


(defn avg-drinks-price
  []
  (int (/ (total-spend) (total-drinks))) )


(defn avg-drinks-price-session
  []
  (map 
    (fn[s] [(:date s) (/ (Float/parseFloat (:cost s)) (Integer/parseInt (:unit s)))]) 
    (for-current-user)))


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
