(ns sponge-clj.items
  (:require [sponge-clj.sponge :as sp]
            [sponge-clj.text :as t]
            [sponge-clj.world :as w]
            [sponge-clj.entity :as e]
            [sponge-clj.logger :as log]
            [sponge-clj.keys :as k])
  (:import (org.spongepowered.api.item ItemType ItemTypes)
           (org.spongepowered.api.item.inventory ItemStack)
           (org.spongepowered.api.data.key Keys)
           (org.spongepowered.api.data DataQuery DataContainer)
           (org.spongepowered.api.entity EntityTypes Item)))

(defn item-type
  "Returns ItemType for give id, or nil if there is no item with this id"
  [id]
  (let [type (sp/get-catalog-type ItemType id)]
    (if (nil? type)
      (do (log/warn (str "Bad material id: " id))
          ItemTypes/AIR)
      type))
  )

(defn- apply-raw-data
  [^ItemStack is raw-data]
  (let [^DataContainer dc (-> is
                           (.toContainer))
        _ (doseq [re raw-data]
            (-> dc
                (.set (DataQuery/of \. (key re)) (val re))))]
    (-> (ItemStack/builder)
        (.fromContainer dc)
        (.build))))

(defn- apply-opts
  [^ItemStack is opts]
  (let [raw-data (get opts :raw-data {})
        opts (dissoc opts :raw-data)]
    (do (doseq [opt opts]
          (k/apply-key is (key opt) (val opt)))
        (apply-raw-data is raw-data))))

(defn item-stack
  "Returns item stack for given id and amount"
  ([id] (item-stack id 1))
  ([id amount] (item-stack id amount {}))
  ([id amount opts]
   (when-let [item-type (item-type id)]
     (let [is (-> (ItemStack/builder)
                  (.itemType item-type)
                  (.quantity amount)
                  (.build))]
       (apply-opts is opts)))))

(defn add-display-name
  [^ItemStack is display-name]
  {:pre [(some? is)
         (some? display-name)]}
  (.offer is Keys/DISPLAY_NAME (t/text display-name))
  is)

(defn add-lore
  [^ItemStack is lore]
  {:pre [(some? is)
         (some? lore)]}
  (.offer is Keys/ITEM_LORE (map t/text lore))
  is)

(defn add-item-stack
  [^Item item ^ItemStack is]
  (.offer item Keys/REPRESENTED_ITEM (.createSnapshot is))
  item)

(defn spawn-item
  [loc ^ItemStack is]
  (let [amount (-> is
                   (.getQuantity))]
    (cond (> amount 0) (-> (w/as-sponge-location loc)
                           (.createEntity EntityTypes/ITEM)
                           (add-item-stack is)
                           (e/spawn (w/get-world loc))))))

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