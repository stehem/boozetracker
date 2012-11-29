(ns boozetracker.models.user
  (:require [boozetracker.db :as db]
            [noir.util.crypt :as crypt]
            [noir.session :as session]
            [noir.validation :as vali])
  (:use [somnium.congomongo]))


(defn username-free?
  [username]
  (with-mongo db/conn
    (nil? (fetch-one :users :where {:username username})) ))


(defn create
  [user]
  (with-mongo db/conn
    (insert! :users {:username (:username user) :password (crypt/encrypt (:password user)) :costs []})))

(defn destroy
  [username]
  (destroy! :users {:username username}))


(defn ^:dynamic current-user
  []
  (db/conn
    (or (collection-exists? :users)
      (create-collection! :users))
    (fetch-one :users :where {:_id (session/get :user-id)})))


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

