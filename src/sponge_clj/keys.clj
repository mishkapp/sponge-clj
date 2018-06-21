(ns sponge-clj.keys
  (:require [clojure.string :as s])
  (:import (org.spongepowered.api.data.key Keys)))

(def sponge-keys
  (let [fields (-> Keys
                   (.getFields))
        redf #(assoc %1
                (-> %2
                    (.getName)
                    (s/lower-case)
                    (s/replace "_" "-")
                    (keyword))
                (-> %2
                    (.get nil)))]
    (reduce redf {} fields)))

(defn apply-key
  "Apply sponge key to object"
  [obj key val]
  {:pre (contains? sponge-keys key)}
  (try
    (do (-> obj
            (.offer (get sponge-keys key) val))
        obj)
    (catch Exception e
      obj)))
