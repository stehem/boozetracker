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
      (include-css "/css/reset.css")]
      (include-css "//ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery.ui.all.css")
    [:body
      (if (User/logged-in?) (link-to "/session/delete" "signout"))
      [:div#username (:username (User/current-user))]
      [:div#wrapper
        content]]))


(defpartial error-item [[first-error]]
  [:div.error first-error])

