(ns user-management.users
  (:require [user-management.db.core :refer [*db*]]
            [conman.core :as conman]
            [buddy.hashers :as hashers]
            [clojure.tools.logging :as log]
            [ring.util.http-response :as response]
            [bouncer.core :as b]
            [bouncer.validators :as v]))

(mount.core/start #'user-management.config/env #'user-management.db.core/*db*)
(conman/bind-connection *db* "sql/queries.sql")

(defn errors->str [h]
  (->> h
       (mapv (comp first last))
       (clojure.string/join "\n")))

(defn signup-errors [{:keys [confirm] :as h}]
  (-> h
      (b/validate 
       :email [v/required v/email]
       :pass [v/required
              [v/min-count 7 :message "password min size is 7 chars."]
              [= confirm :message "password and confirmation does not match, re-enter!"]])
      first))

(defn add-user! [{:keys [session]} user]
  (try
    (let [signup-res (signup-errors user)]
      (if signup-res
        (response/precondition-failed {:msg (errors->str signup-res)})
        (let [{:keys [first_name last_name email pass]} user
              hashed (hashers/derive pass {:alg :pbkdf2+sha256})
              updated-user (-> user
                               (assoc :first_name first_name)
                               (assoc :last_name last_name)
                               (dissoc :confirm)
                               (assoc
                                :pass hashed))]
          (if (seq (find-user-by-email *db* user))
            (-> {:msg "email is already registered!"}
                (response/conflict))
            (let [msg {:msg "user was added successfully!"}
                  location nil]
              (create-user! updated-user)
              (-> (response/created location msg)
                  (assoc :session (assoc session :identity email))))))))
    (catch Exception e 
      (log/error e)
      (response/internal-server-error {:msg "internal server error!"}))))

(defn decoding [encoded]
  (-> (->> (clojure.string/split encoded #" ")
           second
           (.decode (java.util.Base64/getDecoder)))
      (String. (java.nio.charset.Charset/forName "UTF-8"))
      (clojure.string/split #":")))

(defn authenticate [[email password]]
  (let [user (->> {:email email} find-user-by-email first)]
    (if user
      (if (hashers/check password (:pass user) {:alg :pbkdf2+sha256})
        email))))

(defn login! [{:keys [session]} auth]
  (if-let [email (->> auth decoding authenticate)]
    (-> {:msg "login successfully."}
        (response/ok)
        (assoc :session (assoc session :identity email)))
    (response/unauthorized {:msg "login failure."})))

