(ns boozetracker.views.boozetracker
  (:require [noir.util.crypt :as crypt]
            [boozetracker.views.common :as common]
            [noir.response :as response]
            [noir.validation :as vali]
            [noir.session :as session]
            [boozetracker.db :as db]
            [boozetracker.models.session :as Session]
            [boozetracker.models.user :as User])
  (:use [noir.core]
        [somnium.congomongo]
        [hiccup.form-helpers]
        [hiccup.page-helpers]
        [hiccup.core]))


(defmacro defpage-w-auth 
  [path params & body]
  `(defpage ~path ~params
    (if (session/get :user-id)
      ~@body
      (response/redirect "/session/new")) ))



(defpartial error-item [[first-error]]
  [:div.error first-error])



(defpage-w-auth "/tabs" {:as tab}
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







;put auth back on
(defpage [:post, "/costs"] {:as tab}
  (with-mongo db/conn
    (if (= 1 1)
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




(defpage "/user/new" {:as user}
  (html
    (form-to [:post "/users"]
      [:div#user-form-username
        (vali/on-error :username error-item)
        (label "username" "Username")
        (text-field "username" (:username user))]
      [:div#user-form-password
        (vali/on-error :password error-item)
        (label "password" "password")
        (password-field "password")]
      (submit-button "Register")  )))





(defpage [:post "/users"] {:as user}
  (if (User/valid? user)
    (with-mongo db/conn
      (do
        (insert! :users {:username (:username user) :password (crypt/encrypt (:password user))}))
        "success")
      (render "/user/new" user) ) )


(defpage "/session/new" {:as user}
  (html
    (form-to [:post "/sessions"]
      [:div#session-form-username
        (label "username" "Username")
        (text-field "username" (:username user))]
      [:div#session-form-username
        (label "password" "password")
        (password-field "password")]
      (submit-button "Login")  
      (for [attr [:username :password]] (vali/on-error attr error-item))  )))


(defpage [:post "/sessions"] {:as session}
  (if (Session/valid? session)
  (with-mongo db/conn
    (let [user (fetch-one :users :where {:username (:username session)})
      {id :_id login :username pw :password} user]
        (do
          (session/put! :user-id id)
          (session/put! :user-name login)
          (response/redirect "/tabs") )
      ) )
    
    (render "/session/new" session)
    )
)

     ;(str (fetch-by-id "users" (object-id "504487fc65d110d68de92a16"))) 
