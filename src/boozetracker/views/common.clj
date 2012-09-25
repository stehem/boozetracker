(ns boozetracker.views.common
  (:require [boozetracker.models.user :as User]
            [noir.session :as session])
  (:use [noir.core]
        [boozetracker.utils]
        [hiccup.page-helpers]))

(defpartial layout [& content]
            (html5
              [:head
               [:title "boozetracker"]
               (include-css "/css/reset.css")]
              [:body
               [:div#wrapper
                content]]))


(defpartial layout-w-auth [& content]
  (html5
    [:head
      [:title "boozetracker"]
      (include-js "//ajax.googleapis.com/ajax/libs/jquery/1.8.0/jquery.min.js")
      (include-js "/javascript/jquery-ui-1.8.23.custom.min.js")
      (include-js "/javascript/jquery.jeditable.mini.js")
      (include-js "/javascript/jquery.jeditable.datepicker.js")
      (include-js "/javascript/app.js")
      (include-js "https://www.google.com/jsapi")
      (include-css "//netdna.bootstrapcdn.com/twitter-bootstrap/2.1.1/css/bootstrap-combined.min.css")
      (include-css "/css/jquery-ui-1.8.23.custom.css")
      ;(include-css "/css/reset.css")
      "<link href='http://fonts.googleapis.com/css?family=Syncopate' rel='stylesheet' type='text/css'>"
      "<link href='http://fonts.googleapis.com/css?family=Droid+Sans' rel='stylesheet' type='text/css'>"
      (include-css "/css/app.css")]
    [:body
      [:div#header
        (link-to "/"
          [:div#logo-img
            (image "/images/beer2.png")]
          [:div#logo-txt
            "Boozetracker"] )  
        (if (User/current-user)
          [:div#logout
          (:username (User/current-user)) " / "
          (link-to "/session/delete" "logout")]  )
      ]

      [:div#content
        [:div.container-fluid
          (if (User/current-user)
          [:div#nav-top.navbar-inner
            [:div.navbar
              [:ul {:class "nav"}
               [:li {:class (url-active? "/stats")} (link-to "/stats" "Stats")]
               [:li {:class (url-active? "/cost/new")} (link-to "/cost/new" "New")]
               [:li {:class (url-active? "/cost/edit")} (link-to "/cost/edit" "Edit")]
              ]
            ] ]
          )
          [:div#wrapper
            content
          ]
        ] 
      ]
      [:div#footer]
    ]))


(defpartial error-item [[first-error]]
  first-error)

