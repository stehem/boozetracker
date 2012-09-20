(ns boozetracker.views.common
  (:require [boozetracker.models.user :as User]
            [noir.session :as session])
  (:use [noir.core]
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
      (include-js "//ajax.googleapis.com/ajax/libs/jqueryui/1.8.23/jquery-ui.min.js")
      (include-js "/javascript/app.js")
      (include-js "https://www.google.com/jsapi")
      (include-css "//netdna.bootstrapcdn.com/twitter-bootstrap/2.1.1/css/bootstrap-combined.min.css")
      (include-css "//ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery.ui.all.css")
      ;(include-css "/css/reset.css")
      "<link href='http://fonts.googleapis.com/css?family=Syncopate' rel='stylesheet' type='text/css'>"
      "<link href='http://fonts.googleapis.com/css?family=Droid+Sans' rel='stylesheet' type='text/css'>"
      (include-css "/css/app.css")]
    [:body
      [:div#header
        [:div#logo-img
          (image "/images/beer2.png")]
        [:div#logo-txt
          "Boozetracker"]
        [:div#logout
         (:username (User/current-user)) " / "
         (if (User/logged-in?) (link-to "/session/delete" "logout"))]
      ]

      [:div.container-fluid
        [:div#wrapper
          content
        ]
      ] 
    ]))


(defpartial error-item [[first-error]]
  first-error)

