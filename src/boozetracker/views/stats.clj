(ns boozetracker.views.stats
  (:require [boozetracker.views.common :as common]
            [noir.response :as response]
            [noir.validation :as vali]
            [boozetracker.db :as db]
            [boozetracker.models.cost :as Cost]
            [boozetracker.models.stat :as Stat]
            [boozetracker.models.user :as User])
  (:use [noir.core]
        [somnium.congomongo]
        [hiccup.form-helpers]
        [hiccup.page-helpers]
        [boozetracker.utils]
        [hiccup.core]))


(defpage-w-auth "/stats" []
    
    
    
                
               ) 
