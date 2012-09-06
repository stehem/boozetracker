(ns boozetracker.models.cost
  (:require [boozetracker.db :as db]
            [noir.util.crypt :as crypt]
            [noir.validation :as vali])
  (:use [somnium.congomongo]
        [boozetracker.utils]))



(defn valid? [{:keys [date type cost]}]
  (vali/rule (vali/has-value? date)
    [:date "Date required"])
  (vali/rule (vali/has-value? type)
    [:type "Drink required"])
  (vali/rule (vali/has-value? cost)
    [:cost "Cost required"])
  (vali/rule (is-numeric? cost)
    [:cost "Wrong value"])
  (not (vali/errors? :date :type :cost)))

