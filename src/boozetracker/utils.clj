(ns boozetracker.utils
  (:require [noir.validation :as vali])
  (:use [noir.core]
        [clojure.string]))


(defmacro defpage-w-auth 
  [path params & body]
  `(defpage ~path ~params
    (if (User/current-user)
      ~@body
      (response/redirect "/session/new")) ))


(defn is-integer? 
  [cost]
  (and cost (= (count (seq (re-find #"\d+" (trim cost)))) (count (seq (trim cost))))))


(defn is-float? 
  [cost]
  (and cost (= (count (seq (re-find #"\d+\.*\d*" (trim cost)))) (count (seq (trim cost))))))


(defn has-errors? 
  [field]
  (not (empty? (vali/get-errors field))))


