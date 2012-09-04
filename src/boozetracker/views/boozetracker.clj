(ns boozetracker.views.boozetracker
  (:require [noir.util.crypt :as crypt]
            [boozetracker.views.common :as common]
            [noir.response :as response]
            [noir.validation :as vali]
            [noir.session :as session])
  (:use [noir.core]
        [somnium.congomongo]
        [hiccup.form-helpers]
        [hiccup.page-helpers]
        [hiccup.core]))


(def conn
  (make-connection "beertabs"
                   :host "127.0.0.1"
                   :port 27017))


(defmacro defpage-w-auth 
  [path params & body]
  `(defpage ~path ~params
    (if (session/get :user-id)
      ~@body
      (response/redirect "/session/new")) ))


(defn valid? [{:keys [date type cost username password]}]
  (vali/rule (vali/has-value? date)
             [:date "Need a date to remember this one"])
  (vali/rule (vali/has-value? type)
             [:type "Need to know how to remember this"])
  (vali/rule (vali/has-value? cost)
             [:cost "Need to know how painful this was"])
  (not (vali/errors? :date :type :cost)))


(defpartial error-item [[first-error]]
  [:p.error first-error])



(defpage "/tabs" {:as tab}
  (html
    (common/layout-w-auth
    [:div#cost-form 
     (form-to [:post "/costs"]
      [:div#cost-date
       (vali/on-error :date error-item)
        (label "date" "Date of boozing")
        (text-field {:id "date"} "date" (:date tab))]
      [:div#cost-type-hdr 
        (label "cost-type" "weapon of choice")]
      [:div#cost-types
        (vali/on-error :type error-item)
        (label "type" "beer")
        (radio-button "type" (= "beer" (:type tab)) "beer")
        (label "type" "liquor")
        (radio-button "type" (= "liquor" (:type tab)) "liquor")
        (label "type" "wine")
        (radio-button "type" (= "wine" (:type tab)) "wine")
        (label "type" "cocktails")
        (radio-button "type" (= "cocktails" (:type tab)) "cocktails")
        (label "type" "bit-of-everything-really")
        (radio-button "type" (= "everything" (:type tab)) "everything")]
      [:div#cost-cost
        (vali/on-error :cost error-item)
        (label "cost" "Amount of damages")
        (text-field {:id "cost-cost"} "cost" (:cost tab))]
        (submit-button "never forget")
     )])  ))





; put auth back on
;(defpage "/tabs" [tab]
  ;(common/layout-w-auth 
    ;(cost-form tab)
    
    ;)
;)


;put auth back on
(defpage [:post, "/costs"] {:as tab}
  (with-mongo conn
    (if (valid? tab)
      (let [user-id (str (session/get :user-id)) user (fetch-by-id "users" (object-id user-id))]
        (if (and user-id user)
          (do
            (insert! "costs" {:user_id user-id :date (:date tab) :cost (:cost tab)})
            "with great success" )
          "with great failure"
        )
      )
    (render "/tabs" tab)
                
                
)))




(defpage "/user/new" []
  (html
    (form-to [:post "/users"]
      (label "username" "Username")
      (text-field "username")
      (label "password" "password")
      (password-field "password")
      (submit-button "Register")  )))


(defpage [:post "/users"] {:keys [username password]}
  (with-mongo conn
    (if (nil? (fetch-one :users :where {:username username}))
      (do 
        (insert! :users {:username username :password (crypt/encrypt password)})
        "success")
      "already exists") ))


(defpage "/session/new" []
  (html
    (form-to [:post "/sessions"]
      (label "username" "Username")
      (text-field "username")
      (label "password" "password")
      (password-field "password")
      (submit-button "Login")  )))


(defpage [:post "/sessions"] {:keys [username password]}
  (with-mongo conn
    (let [user (fetch-one :users :where {:username username})
      {id :_id login :username pw :password} user]
      (if (and user (crypt/compare password pw))
        (do
          (session/put! :user-id id)
          (session/put! :user-name login)
          (response/redirect "/tabs") )
        "fail") )))

     ;(str (fetch-by-id "users" (object-id "504487fc65d110d68de92a16"))) 
