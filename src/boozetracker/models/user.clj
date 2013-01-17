(ns boozetracker.models.user
  (:require [boozetracker.db :as db]
            [noir.util.crypt :as crypt]
            [noir.session :as session]
            [noir.validation :as vali])

  )


(defn find-by-username
  [username]
  (first
    (db/fetch
      ["SELECT *
      FROM users
      WHERE username = ?" username])))
    

(defn username-free?
  [username]
  (nil? (find-by-username username)))

(defn create
  [user]
  (db/insert
    :users {:username (:username user) :password (crypt/encrypt (:password user))}))


(defn ^:dynamic current-user
  []
  (first
    (db/fetch
      ["SELECT *
      FROM users
      WHERE id = ?" (session/get :user-id)])))
    

(defn current-user-id
  []
  (:id (current-user)))


(defn ^:dynamic logged-in?
  []
  (current-user))




(defn valid? [{:keys [username password]}]
  (vali/rule (vali/has-value? username)
    [:username "Username required"])
  (vali/rule (username-free? username)
    [:username "Username already taken"])
  (vali/rule (vali/has-value? password)
    [:password "Password required"])
  (not (vali/errors? :username :password)))

