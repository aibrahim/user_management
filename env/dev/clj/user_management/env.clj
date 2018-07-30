(ns user-management.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [user-management.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[user_management started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[user_management has shut down successfully]=-"))
   :middleware wrap-dev})
