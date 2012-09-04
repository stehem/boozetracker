(ns boozetracker.test.boozetracker
  (:use boozetracker.views.boozetracker)
  (:use clojure.test)
  (:use noir.util.test))



(-> 
  (send-request "/user/new") 
  (has-status 200)
  
  
  ) 
  

(deftest dummy
  (is (= 2 2)) )


