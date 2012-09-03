(ns cljweb.views.common
  (:require [noir.session :as session])
  (:use [noir.core]
        [hiccup.page-helpers :only [include-css html5]]))

(defpartial layout [& content]
            (html5
              [:head
               [:title "cljweb"]
               (include-css "/css/reset.css")]
              [:body
               [:div#wrapper
                content]]))


(defpartial layout-w-auth [& content]
            (html5
              [:head
               [:title "cljweb"]
               (include-css "/css/reset.css")]
              [:body
               [:div#username (str (session/get :user-name))]
               [:div#wrapper
                content]]))
