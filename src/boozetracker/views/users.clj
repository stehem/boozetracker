(ns boozetracker.views.users
  (:require [boozetracker.views.common :as common]
            [noir.validation :as vali]
            [boozetracker.models.user :as User])
  (:use [noir.core]
        [boozetracker.utils]
        [hiccup.form-helpers]
        [hiccup.page-helpers]
        [hiccup.core]))


(defpage "/user/new" {:as user}
  (html
    (common/layout-w-auth 
    [:div#user-form.bt-form
      (form-to {:class "form-horizontal"} [:post "/users"]
        [:legend "Sign up"]

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
              (submit-button {:class "btn btn-info"} "Sign up") ]  ]
        ) 
     ]  )))



(defpage [:post "/users"] {:as user}
  (if (User/valid? user)
    (User/create user)
    (render "/user/new" user) ) )


