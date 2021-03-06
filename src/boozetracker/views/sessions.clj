(ns boozetracker.views.sessions
  (:require 
            [boozetracker.views.common :as common]
            [noir.response :as response]
            [noir.validation :as vali]
            [noir.session :as session]
            [boozetracker.db :as db]
            [boozetracker.models.user :as User]
            [boozetracker.models.session :as Session])
  (:use [noir.core]
        [hiccup.form-helpers]
        [boozetracker.utils]
        [hiccup.page-helpers]
        [hiccup.core]))


(defpage "/session/new" {:as user}
  (html
    (common/layout-w-auth 
    [:div#user-form.bt-form

        (let [flash (session/flash-get)]
        (if flash
            [:div {:class "span8 alert alert-success" :id "flash"} flash]
            ))
        [:div {:class "clear"}]
      (form-to {:class "form-horizontal"} [:post "/sessions"]
        [:legend "Login"]

        [:div {:class (str "control-group" (if (has-errors? :username) " error"))}
          (label {:class "control-label"} "username" "Username")
          [:div.controls
            (text-field "username" (:username user)) 
          [:div.controls]
          [:span.help-inline
            (vali/on-error :username common/error-item)  ]  ] ]

          [:div {:class (str "control-group" (if (has-errors? :password) " error"))}
            (label {:class "control-label"} "password" "Password")
            [:div.controls
              (password-field "password")  
            [:div.controls]
            [:span.help-inline
              (vali/on-error :password common/error-item)  ]  ] ]

          [:div.control-group
            [:div.controls
              (submit-button {:class "btn btn-info"} "Login") ]  ]

          [:div.control-group
            [:div.controls
              (link-to "/user/new" "Register") ]  ]

        ) 
     ]  )))


(defpage [:post "/sessions"] {:as session}
  (if (Session/valid? session)
  (do 
    (let [user (User/find-by-username (:username session))
      {id :id login :username} user]
        (do
          (session/put! :user-id id)
          (session/put! :user-name login)
          (response/redirect "/cost/new") ) ) )
    (response/status 401
      (render "/session/new" session)) ) )


(defpage "/session/delete" []
  (do
    (session/remove! :user-id)
    (response/redirect "/session/new")))
