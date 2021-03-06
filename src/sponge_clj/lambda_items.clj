(ns sponge-clj.lambda-items
  (:require [sponge-clj.cause :as c]
            [sponge-clj.enchantments :refer :all]
            [sponge-clj.items :refer :all]
            [sponge-clj.util :refer :all]
            [sponge-clj.triggers :as t])
  (:import (org.spongepowered.api.item.inventory ItemStack)
           (org.spongepowered.api.entity.living.player Player)
           (org.spongepowered.api.data DataQuery)
           (org.spongepowered.api.event.block InteractBlockEvent$Secondary$MainHand)))

(def ^:private items (atom {}))

(defn- register
  [id item]
  (swap! items assoc id item))

(defn get-item
  "Returns lambda-item if exists, else nil"
  [key]
  (if (contains? @items key)
    (get @items key)
    nil))

(defn as-item-stack
  [id]
  {:pre [(contains? @items id)]}
  (let [item (get @items id)
        item-type (:material item)
        item-stack (item-stack item-type)]
    (cond-> item-stack
            true (add-custom-data ["lambdaItem", "id"] (name id))
            (contains? item :display-name) (add-display-name (:display-name item))
            (contains? item :lore) (add-lore (:lore item))
            (contains? item :enchantments) (add-enchantments (:enchantments item)))))

(defn lambda-item-stack
  [id]
  (as-item-stack id))

(defn get-lambda-item-id
  [^ItemStack item-stack]
  {:pre [(some? item-stack)]}
  (-> item-stack
      (.toContainer)
      (.get (DataQuery/of ["UnsafeData" "lambdaItem" "id"]))
      (.orElse nil)
      (keyword)))

(defn process-item-use
  [event]
  {:pre [(some? event)]}
  (when-let* [player     (c/first-in (:cause event) Player)
              item-stack (-> player
                             (.getItemInHand (.getHandType (:event event)))
                             (.orElse nil))
              lambda-id   (get-lambda-item-id item-stack)
              lambda-item (get @items lambda-id)
              action      (:action-fn lambda-item)]
    (apply action [player item-stack])))

(defn def-item
  [& {:keys [id material] :as item}]
  {:pre [(some? id)
         (some? material)]}
  (register id item))

(t/def-trigger
  :id :lambda-items-use
  :event-type InteractBlockEvent$Secondary$MainHand
  :predicate #(some? (c/first-in (:cause %) Player))
  :action #(process-item-use %)
  :delay 0
  )
