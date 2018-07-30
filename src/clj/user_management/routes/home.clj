(ns user-management.routes.home
  (:require [user-management.layout :as layout]
            [user-management.db.core :as db]
            [user-management.users :refer :all]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

(defn home-page []
  (layout/render "home.html"))

(defroutes home-routes
  (GET "/" []
       (home-page))
  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
           (response/header "Content-Type" "text/plain; charset=utf-8")))
  (POST "/user/login" request 
                               (try
                                 (let [headers (:headers request)
                                       auth (get headers "authorization")]
                                   (login! request auth))
                                 (catch Exception e {"msg" "something wrong."})))
  (POST "/user/logout" [] 
                           (-> {"msg" "logout successfully."}
                               (response/ok)
                               (assoc :session nil))))

