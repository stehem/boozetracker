(ns boozetracker.models.cost
  (:require [boozetracker.db :as db]
            [noir.util.crypt :as crypt]
            [noir.validation :as vali])
  (:use [somnium.congomongo]
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

