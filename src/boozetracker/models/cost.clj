(ns boozetracker.models.cost
  (:require [boozetracker.db :as db]
            [boozetracker.models.stat :as Stat]
            [boozetracker.models.user :as User]
            [noir.util.crypt :as crypt]
            [noir.validation :as vali])
  (:use 
        [boozetracker.utils]))



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


(defn update
  [param id]
  (db/update
    :costs ["WHERE user_id = ? AND id = ?" (User/current-user-id) (Integer/parseInt id)] param))
    

(defn update-destroy
  [id]
  (db/delete
    :costs ["WHERE user_id = ? AND id = ?" (User/current-user-id) (Integer/parseInt id)]))

