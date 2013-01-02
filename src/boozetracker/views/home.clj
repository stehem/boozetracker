(ns boozetracker.views.home
  (:require [boozetracker.views.common :as common]
            [noir.validation :as vali]
            [noir.response :as response]
            [boozetracker.models.user :as User])
  (:use [noir.core]
        [boozetracker.utils]
        [hiccup.form-helpers]
        [hiccup.page-helpers]
        [hiccup.core]))

(defpage "/" []
  (if (empty? (User/current-user))
    (common/layout-w-auth
      [:div {:id "home"}
       [:p [:span {:id "home-header"} 
            "Boozetracker lets you track how much you spend on drinks and how much booze you consume over time."
            ]]
       [:div {:id "home-box-wrapper"}
        [:div {:class "home-box"}
         [:div {:class "home-box-title"} "Drink"]
         (image "/images/step1.png")
         ]
        [:div {:class "home-box"}
         [:div {:class "home-box-title"} "Track it"]
         (image "/images/step2.png")
         ]
        [:div {:class "home-box"}
         [:div {:class "home-box-title"} "See stats"]
         (image "/images/step3.png")
         ]
        ]
       [:div {:class "clear"}]
       [:div {:id "home-links"} 
        (link-to "/user/new" "Register")
        " / "
        (link-to "/session/new" "Login")
        ]
       ])
    (response/redirect "/stats")))
