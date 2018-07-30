(ns user-management.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[user_management started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[user_management has shut down successfully]=-"))
   :middleware identity})
