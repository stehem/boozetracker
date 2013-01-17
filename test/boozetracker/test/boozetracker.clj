(ns boozetracker.test.boozetracker
  (:require [boozetracker.db :as db]
            [boozetracker.models.user :as User]
            [clojure.java.jdbc :as sql]
            [boozetracker.models.stat :as Stat])
  (:use [boozetracker.views.users]
        [boozetracker.views.sessions]
        [boozetracker.views.costs]
        [clojure.test]
        [noir.response]
        [noir.session]
        [noir.util.test2]))


(def mock-user
  {:username "dummy" :password "dummy" })


(defmacro deftest-w-mock
  [fname & body]
  `(deftest ~fname
    (binding [
              User/logged-in? (fn[] true) 
              User/current-user (fn[] (first (db/fetch ["SELECT * FROM users ORDER BY id DESC LIMIT 1"])))
              Stat/number-of-days-between-now-and-first (fn[] 10)
              ] 
      (sql/with-connection (System/getenv "DATABASE_URL")
        (sql/delete-rows :users ["id IS NOT NULL"])
        (sql/delete-rows :costs ["id IS NOT NULL"]))
      ~@body
    )))

(deftest-w-mock test-new-user
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
      (let [cnt (:count (first (db/fetch ["SELECT COUNT(*) FROM users"])))]
        (-> 
          (send-request [:post "/users"] {"username" "dummy" "password" "dummy"})
          (has-status 302)
          (redirects-to "/session/new")
          (is (= (inc cnt) (:count (first (db/fetch ["SELECT COUNT(*) FROM users"])))))
          (!body-contains #"Username required")
          (!body-contains #"Password required")  )  )
      (-> 
        (send-request [:post "/users"] {"username" "dummy" "password" "dummy"})
        (has-status 200)
        (body-contains #"Username already taken")
        (!body-contains #"Username required")
        (!body-contains #"Password required")  )  ) )
              
        
(deftest-w-mock test-new-session
    (with-noir
      (->
        (send-request "/session/new")
        (has-status 200)
        (has-tags [
          [:input {:type "text" :name "username" :id "username"}]
          [:input {:type "password" :name "password" :id "password"}]
          [:form {:action "/sessions"}] ]) )   
      (-> 
        (send-request [:post "/sessions"] {"username" nil "password" nil})
        (has-status 401)
        (body-contains #"Username required")
        (body-contains #"Password required")  )
      (-> 
        (send-request [:post "/sessions"] {"username" "dummy" "password" nil})
        (has-status 401)
        (!body-contains #"Username required")
        (body-contains #"Password required")  )
      (-> 
        (send-request [:post "/sessions"] {"username" nil "password" "dummy"})
        (has-status 401)
        (body-contains #"Username required")
        (!body-contains #"Password required")  )
      (-> 
        (send-request [:post "/sessions"] {"username" "dummy" "password" "dummy"})
        (has-status 401)
        (body-contains #"Authentication failed")
        (!body-contains #"Username required")
        (!body-contains #"Password required")  )
      (User/create {:username "dummy" :password "dummy"})
      (-> 
        (send-request [:post "/sessions"] {"username" "dummy" "password" "dummy"})
        (has-status 302)
        (redirects-to "/cost/new")))) 
  

(deftest test-costs
        (with-noir
          (->
            (send-request "/cost/new") 
            (has-status 302)
            (redirects-to "/session/new") )))


(deftest-w-mock test-costs2
      (with-noir
        (User/create {:username "dummy" :password "dummy"})
        (->
          (send-request "/cost/new") 
          (has-status 200)
          (body-contains #"dummy")
          (has-tags [
            [:input {:type "text" :name "date" :id "date"}]
            [:input {:type "radio" :name "type" :id "type-beer" :value "beer"}]
            [:input {:type "radio" :name "type" :id "type-liquor" :value "liquor"}]
            [:input {:type "radio" :name "type" :id "type-wine" :value "wine"}]
            [:input {:type "radio" :name "type" :id "type-cocktails" :value "cocktails"}]
            [:input {:type "radio" :name "type" :id "type-everything" :value "everything"}]
            [:form {:action "/costs"}] ]) )
        (->
          (send-request [:post "/costs"]) 
          (body-contains #"Date required")
          (body-contains #"Drink required")
          (body-contains #"Cost required")  )
        (->
          (send-request [:post "/costs"] {"date" "01-01-2012"}) 
          (!body-contains #"Date required")
          (body-contains #"Drink required")
          (body-contains #"Cost required")  )
        (->
          (send-request [:post "/costs"] {"date" "01-01-2012" "type" "beer"}) 
          (!body-contains #"Date required")
          (!body-contains #"Drink required")
          (body-contains #"Cost required")  )
        (->
          (send-request [:post "/costs"] {"type" "beer" "cost" "25"}) 
          (body-contains #"Date required")
          (!body-contains #"Drink required")
          (!body-contains #"Cost required")  )
        (->
          (send-request [:post "/costs"] {"type" "beer" "cost" "qwe"}) 
          (body-contains #"Date required")
          (body-contains #"Number of drinks required")
          (body-contains #"Wrong value") )
        (->
          (send-request [:post "/costs"] {"type" "beer" "cost" "123e"}) 
          (body-contains #"Date required")
          (body-contains #"Number of drinks required")
          (body-contains #"Wrong value") )
        (->
          (send-request [:post "/costs"] {"type" "beer" "cost" "123.00"}) 
          (body-contains #"Date required")
          (body-contains #"Number of drinks required")
          (!body-contains #"Wrong value") )
        (->
          (send-request [:post "/costs"] {"date" "2012-01-01" "type" "beer" "cost" "123 "}) 
          (body-contains #"Number of drinks required")
          (!body-contains #"Wrong value") )
        (->
          (send-request [:post "/costs"] {"date" "2012-01-01" "type" "beer" "cost" "123" "unit" "3"}) 
          (!body-contains #"Wrong value") )
        (->
          (send-request [:post "/costs"] {"date" "2012-01-01" "type" "beer" "cost" "123" "unit" "3q"}) 
          (body-contains #"Wrong value") )
        (->
          (send-request [:post "/costs"] {"date" "2012-01-01" "type" "beer" "cost" "6.5"}) 
          (body-contains #"Number of drinks required")
          (!body-contains #"Wrong value") )

        (db/insert :users mock-user)
        (let [id (:id (first (db/fetch ["SELECT * FROM users ORDER BY id DESC LIMIT 1"])))]
          (is (= 0 (:count (first (db/fetch ["SELECT COUNT(*) FROM costs WHERE user_id = ?" id])))))
          (send-request [:post "/costs"] {"date" "2012-01-01" "type" "beer" "cost" "123" "unit" "2"}) 
            (is (= 1 (:count (first (db/fetch ["SELECT COUNT(*) FROM costs WHERE user_id = ?" id])))))
            (send-request [:post "/costs"] {"date" "2012-01-01" "type" "wine" "cost" "234" "unit" "2"}) 
              (is (= 2 (:count (first (db/fetch ["SELECT COUNT(*) FROM costs WHERE user_id = ?" id]))))))))  
 


(deftest-w-mock test-pie-chart
      (User/create {:username "dummy" :password "dummy"})
      (send-request [:post "/costs"] {"date" "2012-01-01" "type" "beer" "cost" "10" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-01-03" "type" "wine" "cost" "20" "unit" "2"}) 
      (is (= "['wine', 20.0],['beer', 10.0]" (Stat/chart-type)))
      (send-request [:post "/costs"] {"date" "2012-02-01" "type" "beer" "cost" "10" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-03-01" "type" "liquor" "cost" "30" "unit" "2"}) 
      (is (= "['beer', 20.0],['wine', 20.0],['liquor', 30.0]" (Stat/chart-type))))  


(deftest-w-mock test-days-chart
      (User/create {:username "dummy" :password "dummy"})
      (send-request [:post "/costs"] {"date" "2012-09-01" "type" "beer" "cost" "10" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-09-14" "type" "wine" "cost" "20" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-09-15" "type" "wine" "cost" "5" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-09-20" "type" "beer" "cost" "10" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-09-25" "type" "liquor" "cost" "30" "unit" "2"}) 
      (is (= "['Tue', 30.0],['Thu', 10.0],['Fri', 20.0],['Sat', 15.0]" (Stat/pie-chart-day)))) 

(deftest-w-mock test-total-spend
      (User/create {:username "dummy" :password "dummy"})
      (send-request [:post "/costs"] {"date" "2012-09-01" "type" "beer" "cost" "10" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-09-14" "type" "wine" "cost" "20" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-09-15" "type" "wine" "cost" "5" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-09-20" "type" "beer" "cost" "10" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-09-25" "type" "liquor" "cost" "30" "unit" "2"}) 
      (is (= 75.0 (Stat/total-spend))))

(deftest-w-mock test-spend-month
      (User/create {:username "dummy" :password "dummy"})
      (send-request [:post "/costs"] {"date" "2012-09-01" "type" "beer" "cost" "10" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-09-14" "type" "wine" "cost" "20" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-10-20" "type" "beer" "cost" "10" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-10-25" "type" "liquor" "cost" "30" "unit" "2"}) 
      (is (= "['Sep12', 30.0],['Oct12', 40.0]"  (Stat/column-chart-month))))

(deftest-w-mock test-spend-date
      (User/create {:username "dummy" :password "dummy"})
      (send-request [:post "/costs"] {"date" "2012-09-01" "type" "beer" "cost" "10" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-09-14" "type" "wine" "cost" "20" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-10-20" "type" "beer" "cost" "10" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-10-25" "type" "liquor" "cost" "30" "unit" "2"}) 
      (is (= "['01-09-12', 10.0],['14-09-12', 20.0],['20-10-12', 10.0],['25-10-12', 30.0]"  (Stat/line-chart-date))))


(deftest-w-mock test-avg-spend-session
      (User/create {:username "dummy" :password "dummy"})
      (send-request [:post "/costs"] {"date" "2012-09-01" "type" "beer" "cost" "8" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-09-14" "type" "wine" "cost" "9" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-09-20" "type" "beer" "cost" "11" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-09-25" "type" "liquor" "cost" "12" "unit" "2"}) 
      (is (= "10.00" (Stat/average-spend-by-session))))


(deftest-w-mock test-avg-spend-day
      (User/create {:username "dummy" :password "dummy"})
      (send-request [:post "/costs"] {"date" "2012-09-01" "type" "beer" "cost" "5" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-09-03" "type" "wine" "cost" "5" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-09-05" "type" "beer" "cost" "5" "unit" "2"}) 
      (send-request [:post "/costs"] {"date" "2012-09-10" "type" "liquor" "cost" "5" "unit" "2"}) 
      (is (= "2.00" (Stat/average-spend-by-day))))


(deftest-w-mock test-avg-drinks
      (User/create {:username "dummy" :password "dummy"})
      (send-request [:post "/costs"] {"date" "2012-09-01" "type" "beer" "cost" "2.5" "unit" "1"}) 
      (send-request [:post "/costs"] {"date" "2012-09-03" "type" "wine" "cost" "5" "unit" "3"}) 
      (send-request [:post "/costs"] {"date" "2012-09-05" "type" "beer" "cost" "7.5" "unit" "1"}) 
      (send-request [:post "/costs"] {"date" "2012-09-10" "type" "liquor" "cost" "7.5" "unit" "3"}) 
      (send-request [:post "/costs"] {"date" "2012-08-01" "type" "beer" "cost" "5" "unit" "1"}) 
      (send-request [:post "/costs"] {"date" "2012-08-03" "type" "wine" "cost" "2.5" "unit" "3"}) 
      (is (= "5.00") (Stat/average-drink-price))
      (is (= "2.00" (Stat/average-number-of-drinks))))


(deftest-w-mock test-edit-get
      (User/create {:username "dummy" :password "dummy"})
      (send-request [:post "/costs"] {"date" "2012-09-01" "type" "beer" "cost" "15" "unit" "3"}) 
      (send-request [:post "/costs"] {"date" "2012-09-03" "type" "wine" "cost" "6" "unit" "1"}) 
      (send-request [:post "/costs"] {"date" "2012-09-05" "type" "beer" "cost" "5" "unit" "1"}) 
      (->
        (send-request "/cost/edit")
        (body-contains #"data-date=.2012-09-01.")
        (body-contains #"data-field=.type.")
        (body-contains #"data-field=.unit.")
        (body-contains #"data-field=.cost.")
        (body-contains #">beer<")
        (body-contains #">15.0<")
        (body-contains #">3<")  ) )


(deftest-w-mock test-edit-post
      (User/create {:username "dummy" :password "dummy"})
      (send-request [:post "/costs"] {"date" "2012-09-01" "type" "beer" "cost" "15" "unit" "3"}) 
      (->
        (send-request "/cost/edit")
        (body-contains #"data-date=.2012-09-01.")
        (body-contains #">2012-09-01<") )
        (let [id (:id (first (db/fetch ["SELECT * FROM costs ORDER BY id DESC LIMIT 1"])))]
          (send-request [:post "/cost/edit"] {"id" (str id) "field" "date" "value" "2012-09-10"})
      (->
        (send-request "/cost/edit")
        (body-contains #"data-date=.2012-09-10.")
        (body-contains #">2012-09-10<") )
     (send-request [:post "/cost/edit"] {"id" (str id) "field" "type" "value" "wine"}) 
      (->
        (send-request "/cost/edit")
        (body-contains #">wine<") )
      (send-request [:post "/cost/edit"] {"id" (str id) "field" "unit" "value" "4"}) 
      (->
        (send-request "/cost/edit")
        (body-contains #">4<") )
      (send-request [:post "/cost/edit"] {"id" (str id) "field" "cost" "value" "12.50"}) 
      (->
        (send-request "/cost/edit")
        (body-contains #">12\.5<") )
      (send-request [:post "/cost/edit"] {"id" (str id) "field" "cost" "value" "12€"}) 
      (->
        (send-request "/cost/edit")
        (body-contains #">12.0<") ) ) )


(deftest-w-mock test-delete
      (User/create {:username "dummy" :password "dummy"})
      (send-request [:post "/costs"] {"date" "2012-09-01" "type" "beer" "cost" "15" "unit" "3"}) 
      (->
        (send-request "/cost/edit")
        (body-contains #"data-date=.2012-09-01.")
        (body-contains #">2012-09-01<") )
      (let [id (:id (first (db/fetch ["SELECT * FROM costs ORDER BY id DESC LIMIT 1"])))]
      (send-request [:post "/cost/delete"] {"id" (str id)}))
      (->
        (send-request "/cost/edit")
        (!body-contains #"data-date=.2012-09-01.")
        (!body-contains #">2012-09-01<") )  )
                
                
