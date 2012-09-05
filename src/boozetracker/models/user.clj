(ns boozetracker.models.user
  (:require [boozetracker.db :as db]
            [noir.validation :as vali])
  (:use [somnium.congomongo]))


(defn username-free
  [username]
  (with-mongo db/conn
    (nil? (fetch-one :users :where {:username username})) ))


(defn valid? [{:keys [username password]}]
  (vali/rule (vali/has-value? username)
    [:username "Username required"])
  (vali/rule (username-free username)
    [:username "Username already taken"])
  (vali/rule (vali/has-value? password)
    [:password "Password required"])
  (not (vali/errors? :username :password)))

