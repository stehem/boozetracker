(ns boozetracker.test.boozetracker
  (:require [boozetracker.db :as db]
            [boozetracker.models.user :as User]
            [boozetracker.models.stat :as Stat])
  (:use [boozetracker.views.users]
        [boozetracker.views.sessions]
        [boozetracker.views.costs]
        [clojure.test]
        [noir.response]
        [noir.session]
        [somnium.congomongo]
        [noir.util.test2]))


(def conn-test
  (make-connection "beertabs-test"
                   :host "127.0.0.1"
                   :port 27017))

(def mock-user
  {:_id 1 :username "dummy" :password "dummy" })


(defmacro deftest-w-mock
  [fname & body]
  `(deftest ~fname
    (binding [User/logged-in? (fn[] true) User/current-user (fn[] mock-user)] 
    (with-redefs [db/conn conn-test] 
    (with-mongo db/conn
    (drop-coll! :users)
      ~@body  )))))


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
      (let [cnt (fetch-count :users)]
        (-> 
          (send-request [:post "/users"] {"username" "dummy" "password" "dummy" "costs" []})
          (has-status 200)
          (is (= (inc cnt) (fetch-count :users)))
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
        (redirects-to "/cost/new") )
          ) ) 
  

(deftest test-costs
  (with-redefs [db/conn conn-test] 
  (with-mongo db/conn
  (drop-coll! :users)
    (with-noir
      (->
        (send-request "/cost/new") 
        (has-status 302)
        (redirects-to "/session/new") )))))


(deftest-w-mock test-costs2
      (with-noir
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
          (send-request [:post "/costs"] {"date" "01/01/2012"}) 
          (!body-contains #"Date required")
          (body-contains #"Drink required")
          (body-contains #"Cost required")  )
        (->
          (send-request [:post "/costs"] {"date" "01/01/2012" "type" "beer"}) 
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
          (body-contains #"Wrong value") )
        (->
          (send-request [:post "/costs"] {"type" "beer" "cost" "123e"}) 
          (body-contains #"Wrong value") )
        (->
          (send-request [:post "/costs"] {"type" "beer" "cost" "123.00"}) 
          (body-contains #"Wrong value") )
        (->
          (send-request [:post "/costs"] {"type" "beer" "cost" "123 "}) 
          (!body-contains #"Wrong value") )
        (insert! :users mock-user)
        (let [user1 (fetch-one :users :where {:_id (:_id mock-user)})]
          (is (= 0 (count (:costs user1))))
          (send-request [:post "/costs"] {"date" "01/01/2012" "type" "beer" "cost" "123"}) 
          (let [user2 (fetch-one :users :where {:_id (:_id mock-user)})]
            (is (= 1 (count (:costs user2))))
            (send-request [:post "/costs"] {"date" "01/02/2012" "type" "wine" "cost" "234"}) 
            (let [user3 (fetch-one :users :where {:_id (:_id mock-user)})]
              (is (= 2 (count (:costs user3)))))))  ))
 

(deftest-w-mock test-pie-chart
      (send-request [:post "/costs"] {"date" "01/01/2012" "type" "beer" "cost" "10"}) 
      (send-request [:post "/costs"] {"date" "01/03/2012" "type" "wine" "cost" "20"}) 
      (is (= "[['beer', 10], ['wine', 20]]" (Stat/format-chart (Stat/pie-chart))))
      (send-request [:post "/costs"] {"date" "01/02/2012" "type" "beer" "cost" "10"}) 
      (send-request [:post "/costs"] {"date" "01/03/2012" "type" "liquor" "cost" "30"}) 
      (is (= "[['beer', 20], ['wine', 20], ['liquor', 30]]" (Stat/format-chart (Stat/pie-chart))))  )


(deftest-w-mock test-days-chart
      (send-request [:post "/costs"] {"date" "01-09-2012" "type" "beer" "cost" "10"}) 
      (send-request [:post "/costs"] {"date" "14-09-2012" "type" "wine" "cost" "20"}) 
      (send-request [:post "/costs"] {"date" "15-09-2012" "type" "wine" "cost" "5"}) 
      (send-request [:post "/costs"] {"date" "20-09-2012" "type" "beer" "cost" "10"}) 
      (send-request [:post "/costs"] {"date" "25-09-2012" "type" "liquor" "cost" "30"}) 
      (is (= "[['Saturday', 15], ['Friday', 20], ['Thursday', 10], ['Tuesday', 30]]" (Stat/format-chart (Stat/days-chart))))  )


(deftest-w-mock test-total-spend
      (send-request [:post "/costs"] {"date" "01-09-2012" "type" "beer" "cost" "10"}) 
      (send-request [:post "/costs"] {"date" "14-09-2012" "type" "wine" "cost" "20"}) 
      (send-request [:post "/costs"] {"date" "15-09-2012" "type" "wine" "cost" "5"}) 
      (send-request [:post "/costs"] {"date" "20-09-2012" "type" "beer" "cost" "10"}) 
      (send-request [:post "/costs"] {"date" "25-09-2012" "type" "liquor" "cost" "30"}) 
      (is (= 75 (Stat/total-spend))))


(deftest-w-mock test-avg-spend-session
      (send-request [:post "/costs"] {"date" "01-09-2012" "type" "beer" "cost" "8"}) 
      (send-request [:post "/costs"] {"date" "14-09-2012" "type" "wine" "cost" "9"}) 
      (send-request [:post "/costs"] {"date" "20-09-2012" "type" "beer" "cost" "11"}) 
      (send-request [:post "/costs"] {"date" "25-09-2012" "type" "liquor" "cost" "12"}) 
      (is (= 10 (Stat/avg-spend-session))))


(deftest-w-mock test-avg-spend-day
      (send-request [:post "/costs"] {"date" "01-09-2012" "type" "beer" "cost" "5"}) 
      (send-request [:post "/costs"] {"date" "03-09-2012" "type" "wine" "cost" "5"}) 
      (send-request [:post "/costs"] {"date" "05-09-2012" "type" "beer" "cost" "5"}) 
      (send-request [:post "/costs"] {"date" "10-09-2012" "type" "liquor" "cost" "5"}) 
      (is (= 2 (Stat/avg-spend-day))))
