(ns boozetracker.models.cost
  (:require [boozetracker.db :as db]
            [boozetracker.models.stat :as Stat]
            [noir.util.crypt :as crypt]
            [noir.validation :as vali])
  (:use 
        [boozetracker.utils]))

(use 'korma.core)

(defentity costs
  (pk :id)
  (table :costs)
  (entity-fields :cost :type :unit :date :user_id))


(defn valid? [{:keys [date type cost unit]}]
  (vali/rule (vali/has-value? date)
    [:date "Date required"])
  (vali/rule (vali/has-value? type)
    [:type "Drink required"])
  (vali/rule (vali/has-value? cost)
    [:cost "Cost required"])
  (vali/rule (is-float? cost)
    [:cost "Wrong value"])
  (vali/rule (vali/has-value? unit)
    [:unit "Number of drinks required"])
  (vali/rule (is-integer? unit)
    [:unit "Wrong value"])

  (not (vali/errors? :date :type :cost :unit)))


(defn update_
  [date attr value]
  (let [old-costs (Stat/for-current-user)
        old-costs-w-idx (map-indexed (fn[idx itm] (merge itm {:idx idx} )) old-costs)
        old-cost (first (filter #(= (:date %) date) old-costs-w-idx))
        new-costs (assoc-in old-costs [(:idx old-cost) (keyword attr)] value)
        ]
    (cond
      (and (= attr "unit") (not (is-integer? value))) false
      (and (= attr "cost") (not (is-float? value))) false
      :else new-costs) ))


(defn update-destroy
  [date]
  (remove #(= (:date %) date) (Stat/for-current-user)))
