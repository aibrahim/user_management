(ns user-management.handler
  (:require [user-management.middleware :as middleware]
            [user-management.layout :refer [error-page]]
            [user-management.routes.home :refer [home-routes]]
            [compojure.core :refer [routes wrap-routes]]
            [ring.util.http-response :as response]
            [compojure.route :as route]
            [user-management.env :refer [defaults]]
            [mount.core :as mount]
            [buddy.auth.backends :as backends]))

(mount/defstate init-app
  :start ((or (:init defaults) identity))
  :stop  ((or (:stop defaults) identity)))

(def backend (backends/session))

(mount/defstate app
  :start
  (-> (routes
       (-> #'home-routes
           (wrap-routes middleware/wrap-csrf)
           (wrap-routes middleware/wrap-formats))
       (route/not-found
        (:body
         (error-page {:status 404
                      :title "page not found"}))))
      middleware/wrap-base))

