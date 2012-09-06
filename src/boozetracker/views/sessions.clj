(ns boozetracker.views.sessions
  (:require 
            [boozetracker.views.common :as common]
            [noir.response :as response]
            [noir.validation :as vali]
            [noir.session :as session]
            [boozetracker.db :as db]
            [boozetracker.models.session :as Session])
  (:use [noir.core]
        [somnium.congomongo]
        [hiccup.form-helpers]
        [hiccup.page-helpers]
        [hiccup.core]))


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
      (for [attr [:username :password]] (vali/on-error attr common/error-item))  )))


(defpage [:post "/sessions"] {:as session}
  (if (Session/valid? session)
  (with-mongo db/conn
    (let [user (fetch-one :users :where {:username (:username session)})
      {id :_id login :username pw :password} user]
        (do
          (session/put! :user-id id)
          (session/put! :user-name login)
          (response/redirect "/cost/new") ) ) )
    (response/status 401
      (common/layout-w-auth (render "/session/new" session))) ) )


(defpage "/session/delete" []
  (do
    (session/remove! :user-id)
    (response/redirect "/session/new")))
