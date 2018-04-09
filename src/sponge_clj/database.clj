(ns sponge-clj.database
  (:require [konserve.filestore :as kfs]
            [konserve.core :as k]
            [clojure.core.async :refer (<!!)]))

(def ^:private storages (atom {}))

(def db-dir "./spongeclj.db/")

(defn get-storage
  [db-key]
  (if (contains? @storages db-key)
    (get @storages db-key)
    (get (swap! storages assoc db-key (<!! (kfs/new-fs-store (str db-dir (name db-key))))) db-key)))

(defn db-exists?
  [db-id key]
  (<!! (k/exists? (get-storage db-id) key)))

(defn db-get-in
  [db-id key-vec]
  (<!! (k/get-in (get-storage db-id) key-vec)))

(defn db-assoc-in
  [db-id key-vec value]
  (<!! (k/assoc-in (get-storage db-id) key-vec value)))

(defn db-update-in
  [db-id key-vec fn]
  (<!! (k/update-in (get-storage db-id) key-vec fn)))

(defn db-dissoc
  [db-id key]
  (<!! (k/dissoc (get-storage db-id) key)))

(defn db-list-keys
  [db-id]
  (<!! (kfs/list-keys (get-storage db-id))))

;(defn dissoc-in
;  "Dissociates an entry from a nested associative structure returning a new
;  nested structure. keys is a sequence of keys. Any empty maps that result
;  will not be present in the new structure."
;  [[k & ks :as keys]]
;  (if ks
;    (if-let [nextmap (get-in [k])]
;      (let [newmap (dissoc-in ks)]
;        (if (seq newmap)
;          (assoc-in [k] newmap)
;          (dissoc k)))
;      (get-in [k]))
;    (dissoc k)))
;
;;;TODO: dissoc-in