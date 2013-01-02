(ns boozetracker.models.session
  (:require [boozetracker.db :as db]
            [boozetracker.models.user :as User]
            [noir.util.crypt :as crypt]
            [noir.validation :as vali]))


(defn authenticate?
  [username password]
  (let [user (User/find-by-username username)]
    (and user (crypt/compare password (:password user)))))


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

