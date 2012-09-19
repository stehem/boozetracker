(ns boozetracker.models.stat
  (:require [boozetracker.db :as db]
            [clojure.string]
            [boozetracker.models.user :as User])
  (:use [clj-time.core :exclude [extend]]
        [clj-time.format]
        [clj-time.coerce]
        [somnium.congomongo]))


(defn for-current-user
  []
  (with-mongo db/conn
    (:costs (fetch-one :users :where {:_id (:_id (User/current-user))}))  ))



(defn grouped-by
  [f]
  (let [grouped f] 
    (for [[type costs] grouped] (into [] [type (reduce + (map #(Integer/parseInt (:cost %)) costs))]))))


(def custom-formatter (formatter "dd-MM-YYYY"))
(def custom-formatter-r (formatter "MM-YYYY"))


(defn to-cljdate
  [d]
  (parse custom-formatter d))


(def week {1 "Monday" 2 "Tuesday" 3 "Wednesday" 4 "Thursday" 5 "Friday" 6 "Saturday" 7 "Sunday"})


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
  (reduce + (reduce (fn[xs x] (conj xs (Integer/parseInt (:cost x)))) [] (for-current-user))))


(defn format-chart
  [pie]
  (format "[%s]" (clojure.string/join ", " (map (fn[p] (format "['%s', %s]" (first p) (second p))) pie))))
    
                       
; TODO number of units glasses, pints




(defn spend-month
  []
  (grouped-by (group-by 
                #(let [date (to-cljdate (:date %))] (format "%s-%s" (month date) (year date))) 
                (for-current-user)  )))


(defn sorted-spend-month
  []
  (sort-by last (map #(conj % (to-long (parse custom-formatter-r (first %)))) (spend-month))))
  
  
(defn spend-day
  []
  (grouped-by (group-by :date (for-current-user)  )))


(defn avg-spend-month
  []
  (/ (total-spend) (count (spend-month))))


(defn avg-spend-session
  []
  (/ (total-spend) (count (spend-day))))


(defn sorted-spend-day
  []
  (sort-by last (map #(conj % (to-long (to-cljdate (first %)))) (spend-day))) )


(defn min-date
  []
  (to-cljdate (ffirst (sorted-spend-day))))

  
(defn max-date
  []
  (to-cljdate (first (last (sorted-spend-day)))))


(defn avg-spend-day
  []
  (let [days (in-days (interval (min-date) (max-date))) spend (total-spend)]
    (int (/ spend days))  ))


