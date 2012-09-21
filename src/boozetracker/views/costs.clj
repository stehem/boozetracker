(ns boozetracker.views.costs
  (:require [boozetracker.views.common :as common]
            [noir.response :as response]
            [noir.validation :as vali]
            [boozetracker.db :as db]
            [boozetracker.models.cost :as Cost]
            [boozetracker.models.user :as User])
  (:use [noir.core]
        [boozetracker.utils]
        [somnium.congomongo] [hiccup.form-helpers]
        [hiccup.page-helpers]
        [hiccup.core]))




(defpartial cost-form [tab]
   (html
    (common/layout-w-auth
    [:div#cost-form.bt-form
     [:legend "Track some drinking"]
     (form-to {:class "form-horizontal"} [:post "/costs"]

      [:div {:class (str "control-group" (if (has-errors? :date) " error"))}
        (label {:class "control-label"} "date" "Date of boozing")
        [:div.controls
          (text-field {:id "date"} "date" (:date tab))
          [:div.controls]
          [:span.help-inline
          (vali/on-error :date common/error-item)  ] ]  ]

      [:div {:class (str "control-group" (if (has-errors? :type) " error"))}
        (label {:class "control-label"} "cost-type" "Weapon of choice")
        [:div.controls
        [:label {:class "radio inline"}
          (radio-button "type" (= "beer" (:type tab)) "beer") "beer" ]
        [:label {:class "radio inline"}
          (radio-button "type" (= "liquor" (:type tab)) "liquor") "liquor"  ]
        [:label {:class "radio inline"}
          (radio-button "type" (= "wine" (:type tab)) "wine") "wine"  ]
        [:label {:class "radio inline"}
          (radio-button "type" (= "cocktails" (:type tab)) "cocktails") "cocktails"  ]
        [:label {:class "radio inline"}
          (radio-button "type" (= "everything" (:type tab)) "everything") "everything" ] 
        [:div.controls]
          [:span.help-inline
            (vali/on-error :type common/error-item)  ] ]  ]

      [:div {:class (str "control-group" (if (has-errors? :cost) " error"))}
        (label {:class "control-label"} "cost" "Amount of damages")
        [:div.controls
          (text-field {:id "cost-cost"} "cost" (:cost tab)) 
          [:div.controls]
          [:span.help-inline
            (vali/on-error :cost common/error-item)  ]  ]  ] 

      [:div {:class (str "control-group" (if (has-errors? :unit) " error"))}
        (label {:class "control-label"} "unit" "Number of drinks")
        [:div.controls
          (text-field {:id "cost-unit"} "unit" (:unit tab)) 
          [:div.controls]
          [:span.help-inline
            (vali/on-error :unit common/error-item)  ]  ]  ] 

      [:div.control-group
        [:div.controls
          (submit-button {:class "btn btn-info"} "never forget") ]  ]
     )])  )         
            
)

(defpage-w-auth "/cost/new" {:as tab}
  (cost-form tab))


(defpage-w-auth [:post, "/costs"] {:as cost}
  (with-mongo db/conn
    (if (Cost/valid? cost)
      (let [user (User/current-user)]
        (if user
          (do
            (update! :users {:_id (:_id user)} {:$push {:costs cost}})
            ;(insert! "costs" {:user_id (:_id user) :date (:date cost) :type (:type cost) :cost (:cost cost)})
            "with great success" )
          "with great failure"  ) )
    (render "/cost/new" cost) ) ) )

