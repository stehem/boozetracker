(ns boozetracker.orm)

(use 'korma.core)

(defentity users
  (pk :id)
  (table :users)
  (entity-fields :username :password))

(defentity costs
  (pk :id)
  (table :costs)
  (entity-fields :cost :type :unit :date :user_id))

