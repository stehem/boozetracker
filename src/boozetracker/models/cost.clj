(ns boozetracker.models.cost
  (:require [boozetracker.db :as db]
            [boozetracker.models.stat :as Stat]
            [boozetracker.models.user :as User]
            [noir.util.crypt :as crypt]
            [noir.validation :as vali])
  (:use 
    [boozetracker.orm]
        [boozetracker.utils]))


(use 'korma.core)

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
  [param id]
  (update costs
    (set-fields param)
    (where {:id (Integer/parseInt id)})
    (where {:user_id (User/current-user-id)})))


(defn update-destroy
  [date]
  (remove #(= (:date %) date) (Stat/for-current-user)))
