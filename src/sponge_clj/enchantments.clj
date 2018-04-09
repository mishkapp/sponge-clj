(ns sponge-clj.enchantments
  (:require [sponge-clj.sponge :as sp]
            [sponge-clj.sponge :as sp])
  (:import (org.spongepowered.api.item.enchantment Enchantment EnchantmentType)
           (org.spongepowered.api.item.inventory ItemStack)
           (org.spongepowered.api.data.key Keys)))

(defn add-enchantments
  "Adds enchantments to item"
  [^ItemStack item, enchantments]
  {:pre [(some? item), (vector? enchantments)]}
  (.offer item Keys/ITEM_ENCHANTMENTS enchantments)
  item)

(defn add-enchantment
  "Adds enchantment to item"
  [^ItemStack item, ^Enchantment ench]
  {:pre [(some? item), (some? ench)]}
  (let [enchantments (-> (.get item Keys/ITEM_ENCHANTMENTS)
                         (.orElse []))]
    (.offer item Keys/ITEM_ENCHANTMENTS (concat enchantments [ench]))
    item))

(defn enchantment-type
  "Retrieves enchantment type by id"
  [id]
  (sp/get-catalog-type EnchantmentType id))

(defn enchantment
  "Creates enchantment"
  [id level]
  (when-let [ench-type (enchantment-type id)]
    (-> (Enchantment/builder)
        (.type ench-type)
        (.level level)
        (.build))))