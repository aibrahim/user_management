(ns user
  (:require [user-management.config :refer [env]]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [mount.core :as mount]
            [user-management.figwheel :refer [start-fw stop-fw cljs]]
            [user-management.core :refer [start-app]]
            [user-management.db.core]
            [conman.core :as conman]
            [luminus-migrations.core :as migrations]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(defn start []
  (mount/start-without #'user-management.core/repl-server))

(defn stop []
  (mount/stop-except #'user-management.core/repl-server))

(defn restart []
  (stop)
  (start))

(defn restart-db []
  (mount/stop #'user-management.db.core/*db*)
  (mount/start #'user-management.db.core/*db*)
  (binding [*ns* 'user-management.db.core]
    (conman/bind-connection user-management.db.core/*db* "sql/queries.sql")))

(defn reset-db []
  (migrations/migrate ["reset"] (select-keys env [:database-url])))

(defn migrate []
  (migrations/migrate ["migrate"] (select-keys env [:database-url])))

(defn rollback []
  (migrations/migrate ["rollback"] (select-keys env [:database-url])))

(defn create-migration [name]
  (migrations/create name (select-keys env [:database-url])))


