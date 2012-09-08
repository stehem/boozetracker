(ns boozetracker.views.costs
  (:require [boozetracker.views.common :as common]
            [noir.response :as response]
            [noir.validation :as vali]
            [boozetracker.db :as db]
            [boozetracker.models.cost :as Cost]
            [boozetracker.models.user :as User])
  (:use [noir.core]
        [boozetracker.utils]
        [somnium.congomongo]
        [hiccup.form-helpers]
        [hiccup.page-helpers]
        [hiccup.core]))


(defpartial cost-form [tab]
   (html
    (common/layout-w-auth
    [:div#cost-form 
     (form-to [:post "/costs"]
      [:div#cost-date
       (vali/on-error :date common/error-item)
        (label "date" "Date of boozing")
        (text-field {:id "date"} "date" (:date tab))]
      [:div#cost-type-hdr 
        (label "cost-type" "weapon of choice")]
      [:div#cost-types
        (vali/on-error :type common/error-item)
        (label "type" "beer")
        (radio-button "type" (= "beer" (:type tab)) "beer")
        (label "type" "liquor")
        (radio-button "type" (= "liquor" (:type tab)) "liquor")
        (label "type" "wine")
        (radio-button "type" (= "wine" (:type tab)) "wine")
        (label "type" "cocktails")
        (radio-button "type" (= "cocktails" (:type tab)) "cocktails")
        (label "type" "bit-of-everything-really")
        (radio-button "type" (= "everything" (:type tab)) "everything")]
      [:div#cost-cost
        (vali/on-error :cost common/error-item)
        (label "cost" "Amount of damages")
        (text-field {:id "cost-cost"} "cost" (:cost tab))]
        (submit-button "never forget")
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

