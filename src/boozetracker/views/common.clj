(ns boozetracker.views.common
  (:require [noir.session :as session])
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
      [:div#username (str (session/get :user-name))]
      [:div#wrapper
        content]]))
