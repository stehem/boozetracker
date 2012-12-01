(ns boozetracker.views.home
  (:require [boozetracker.views.common :as common]
            [noir.validation :as vali]
            [boozetracker.models.user :as User])
  (:use [noir.core]
        [boozetracker.utils]
        [hiccup.form-helpers]
        [hiccup.page-helpers]
        [hiccup.core]))

(defpage "/" []
  (common/layout-w-auth
    [:div {:id "home"}
      [:p [:span {:id "home-header"} 
        "Boozetracker lets you track how much you spend on drinks and how much booze you consume over time."
      ]]
      [:div {:class "home-box"}
        [:span {:class "home-box-title"} "1: Drink"]
        (image "/images/drink-beer-pint.png")
      ]
    ]))
