(ns boozetracker.models.user
  (:require [boozetracker.db :as db]
            [noir.util.crypt :as crypt]
            [noir.session :as session]
            [noir.validation :as vali])

   (:use [boozetracker.orm])
  )

(use 'korma.core)

(defn find-by-username
  [username]
  (first
    (select users
      (where {:username username}))))

(defn username-free?
  [username]
  (not (nil? (find-by-username username))))

(defn create
  [user]
  (insert users
    (values {:username (:username user) :password (crypt/encrypt (:password user))})))

(defn destroy
  [username]
  )


(defn ^:dynamic current-user
  []
  (first
    (select users
      (where {:id (session/get :user-id)}))))


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

