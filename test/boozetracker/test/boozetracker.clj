(ns boozetracker.test.boozetracker
  (:require [boozetracker.db :as db])
  (:use boozetracker.views.boozetracker)
  (:use clojure.test)
  (:use noir.response)
  (:use noir.session)
  (:use somnium.congomongo)
  (:use noir.util.test2))



(deftest test-new-user
  (with-mongo db/conn
    (with-noir
      (->
        (send-request "/user/new")
        (has-status 200)
        (has-tags [
          [:input {:type "text" :name "username" :id "username"}]
          [:input {:type "password" :name "password" :id "password"}]
          [:form {:action "/users"}] ]) )   
      (-> 
        (send-request [:post "/users"] {"username" nil "password" nil})
        (has-status 200)
        (body-contains #"Username required")
        (body-contains #"Password required")  )
      (-> 
        (send-request [:post "/users"] {"username" "dummy" "password" nil})
        (has-status 200)
        (!body-contains #"Username required")
        (body-contains #"Password required")  )
      (-> 
        (send-request [:post "/users"] {"username" nil "password" "dummy"})
        (has-status 200)
        (body-contains #"Username required")
        (!body-contains #"Password required")  )
      (-> 
        (send-request [:post "/users"] {"username" "dummy" "password" "dummy"})
        (has-status 200)
        (!body-contains #"Username required")
        (!body-contains #"Password required")  )
      (-> 
        (send-request [:post "/users"] {"username" "dummy" "password" "dummy"})
        (has-status 200)
        (body-contains #"Username already taken")
        (!body-contains #"Username required")
        (!body-contains #"Password required")  )  )
              
      (destroy! "users" {:username "dummy"})  ) )



  
  
  
  



