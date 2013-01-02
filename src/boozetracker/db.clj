(ns boozetracker.db)

(use 'korma.db)

(defdb pg (postgres {:db "boozetracker"
                       :user "postgres"
                       :password "jajapostgres"
                                }))
