(ns boozetracker.views.users
  (:require [boozetracker.views.common :as common]
            [noir.validation :as vali]
            [boozetracker.models.user :as User])
  (:use [noir.core]
        [hiccup.form-helpers]
        [hiccup.page-helpers]
        [hiccup.core]))


(defpage "/user/new" {:as user}
  (html
    (form-to [:post "/users"]
      [:div#user-form-username
        (vali/on-error :username common/error-item)
        (label "username" "Username")
        (text-field "username" (:username user))]
      [:div#user-form-password
        (vali/on-error :password common/error-item)
        (label "password" "password")
        (password-field "password")]
      (submit-button "Register")  )))



(defpage [:post "/users"] {:as user}
  (if (User/valid? user)
    (User/create user)
    (render "/user/new" user) ) )

