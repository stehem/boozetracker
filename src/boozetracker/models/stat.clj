(ns boozetracker.models.stat
  (:require [boozetracker.db :as db]
            [clojure.string]
            [boozetracker.models.user :as User])
  (:use [somnium.congomongo]))


(defn for-current-user
  []
  (with-mongo db/conn
    (:costs (fetch-one :users :where {:_id (:_id (User/current-user))}))  ))


(defn pie-chart
  []
  (map (fn[grouped] [
                     (first grouped) 
                     (reduce + 
                        (map #(Integer/parseInt (str %))
                          (first 
                            (map (fn[data] (map (fn[d] (:cost d)) data)) (rest grouped))  ) ) )  
                     ]  
        )
        (group-by :type (for-current-user)  ) ) )


(defn format-pie-chart
  [pie]
  (format "[%s]" (clojure.string/join ", " (map (fn[p] (format "['%s', %s]" (first p) (last p))) pie))))
    
                       
