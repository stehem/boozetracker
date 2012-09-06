(ns boozetracker.models.session
  (:require [boozetracker.db :as db]
            [noir.util.crypt :as crypt]
            [noir.validation :as vali])
  (:use [somnium.congomongo]))


(defn authenticate?
  [username password]
  (with-mongo db/conn
    (let [user (fetch-one :users :where {:username username}) pw (:password user)]
      (and user (crypt/compare password pw)))))


(defn valid? [{:keys [username password]}]
  (vali/rule (vali/has-value? username)
    [:username "Username required"])
  (vali/rule (vali/has-value? password)
    [:password "Password required"])
  (if (vali/has-value? password) 
    (if (vali/has-value? username)
      (vali/rule (authenticate? username password)
        [:password "Authentication failed"])))
  (not (vali/errors? :username :password)))

