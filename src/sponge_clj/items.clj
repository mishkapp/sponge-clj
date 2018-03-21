(ns sponge-clj.items
  (:require [sponge-clj.sponge :as sp]
            [sponge-clj.text :as text]
            [sponge-clj.world :as w]
            [sponge-clj.entity :as e])
  (:import (org.spongepowered.api.item ItemType)
           (org.spongepowered.api.item.inventory ItemStack)
           (org.spongepowered.api.data.key Keys)
           (org.spongepowered.api.data DataQuery)
           (org.spongepowered.api.entity EntityTypes Item)))

(defn item-type
  "Returns ItemType for give id, or nil if there is no item with this id"
  [id]
  (sp/get-catalog-type ItemType id))

(defn item-stack
  "Returns item stack for given id and amount"
  ([id] (item-stack id 1))
  ([id amount]
   (when-let [item-type (item-type id)]
     (-> (ItemStack/builder)
         (.itemType item-type)
         (.quantity amount)
         (.build)))))

(defn add-display-name
  [^ItemStack is display-name]
  {:pre [(some? is)
         (some? display-name)]}
  (do (println (str is))
      (println (str display-name))
      (.offer is Keys/DISPLAY_NAME (text/to-text display-name))
      is))

(defn add-lore
  [^ItemStack is lore]
  {:pre [(some? is)
         (some? lore)]}
  (do (.offer is Keys/ITEM_LORE (map text/to-text lore))
      is))

(defn add-enchantment
  [^ItemStack is enchantment]
  {:pre [(some? is)
         (some? enchantment)]}
  (let [enchantments (-> (.get is Keys/ITEM_ENCHANTMENTS)
                         (.orElse []))]
    (do (.offer is Keys/ITEM_ENCHANTMENTS (concat enchantments [enchantment]))
        is)))

(defn add-enchantments
  [^ItemStack is enchantments]
  {:pre [(some? is)
         (some? enchantments)]}
  (do (map (partial add-enchantment is) enchantments)
      is))

(defn add-item-stack
  [^Item item ^ItemStack is]
  (do (.offer item Keys/REPRESENTED_ITEM (.createSnapshot is))
      item))

(defn spawn-item
  [loc ^ItemStack is]
  (-> (w/as-sponge-location loc)
      (.createEntity EntityTypes/ITEM)
      (add-item-stack is)
      (e/spawn (w/get-world loc))))

(defn add-custom-data
  [^ItemStack is path value]
  {:pre [(some? is)
         (and (some? path) (not (empty? path)))
         (some? value)]}
  (let [doc (-> is
                (.toContainer)
                (.set (DataQuery/of (into ["UnsafeData"] path)) value))]
    (-> (ItemStack/builder)
        (.fromContainer doc)
        (.build))))