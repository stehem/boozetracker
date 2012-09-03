(ns cljweb.views.authenticate
  (:require [noir.util.crypt :as crypt]
            [cljweb.views.common :as common]
            [noir.response :as response]
            [noir.session :as session])
  (:use [noir.core]
        [somnium.congomongo]
        [hiccup.form-helpers]
        [hiccup.page-helpers]
        [hiccup.core]))


(def conn
  (make-connection "beertabs"
                   :host "127.0.0.1"
                   :port 27017))


(defmacro defpage-w-auth 
  [path params & body]
  `(defpage ~path ~params
    (if (session/get :user-id)
      ~@body
      (response/redirect "/session/new")) ))


(defpage-w-auth "/tabs" []
  (common/layout-w-auth "tabs")
  
  
)


(defpage "/user/new" []
  (html
    (form-to [:post "/users"]
      (label "username" "Username")
      (text-field "username")
      (label "password" "password")
      (password-field "password")
      (submit-button "Register")  )))


(defpage [:post "/users"] {:keys [username password]}
  (with-mongo conn
    (if (nil? (fetch-one :users :where {:username username}))
      (do 
        (insert! :users {:username username :password (crypt/encrypt password)})
        "success")
      "already exists") ))


(defpage "/session/new" []
  (html
    (form-to [:post "/sessions"]
      (label "username" "Username")
      (text-field "username")
      (label "password" "password")
      (password-field "password")
      (submit-button "Login")  )))


(defpage [:post "/sessions"] {:keys [username password]}
  (with-mongo conn
    (let [user (fetch-one :users :where {:username username})
      {id :_id login :username pw :password} user]
      (if (and user (crypt/compare password pw))
        (do
          (session/put! :user-id id)
          (session/put! :user-name login)
          (response/redirect "/") )
        "fail"
      
      )
              )))

     ;(str (fetch-by-id "users" (object-id "504487fc65d110d68de92a16"))) 
