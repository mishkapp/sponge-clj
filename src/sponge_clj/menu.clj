(ns sponge-clj.menu
  (:require [sponge-clj.items :as it]
            [sponge-clj.text :as t]
            [sponge-clj.cause :as c]
            [sponge-clj.player :as p]
            [sponge-clj.util :as u])
  (:import (org.spongepowered.api.item.inventory Inventory InventoryArchetypes Slot InventoryProperty)
           (org.spongepowered.api.item.inventory.property InventoryTitle InventoryDimension InventoryCapacity SlotPos SlotIndex)
           (org.spongepowered.api.event.item.inventory ClickInventoryEvent)
           (java.util.function Consumer)
           (org.spongepowered.api.entity.living.player Player)))

(defmacro def-menu [name & {:keys [] :as menu}]
  `(def ~name ~menu))

(defn open-menu
  [menu ^Player player]
  "Opens menu for player"
  (let [slots (atom [])
        slots-actions (atom {})
        inv (-> (Inventory/builder)
                (.of (InventoryArchetypes/MENU_GRID))
                (.property (InventoryTitle. (:title menu)))
                (.listener ClickInventoryEvent (reify Consumer
                                                 (accept [this event]
                                                   (do (-> event
                                                           (.setCancelled true))
                                                       (let [index (-> event
                                                                       (.getTransactions)
                                                                       (.get 0)
                                                                       (.getSlot)
                                                                       (.getProperty SlotIndex "slotindex")
                                                                       (.get)
                                                                       (.getValue))
                                                             entry (get @slots-actions index)
                                                             player (c/first-in (-> event
                                                                                    (.getCause))
                                                                                Player)]
                                                         (if (p/has-permission player (:permission entry))
                                                             (apply (:action entry) [player])
                                                             (u/send-message player "You don't have enough permission")))))))
                (.property (InventoryDimension/PROPERTY_NAME) (InventoryDimension. (:lines menu) 9))
                (.build (sponge-clj.core/get-plugin)))
        _ (reset! slots (iterator-seq (-> inv
                                          (.slots)
                                          (.iterator))))
        content (:content menu)
        _ (doseq [[k v] content] (let [x (- (first k) 1)
                                       y (second k)
                                       i (+ (* x 9) y)
                                       slot (nth @slots i)]
                                   (do
                                     (.set slot (.copy (:item v)))
                                     (swap! slots-actions assoc i v))))
        ]
    (.openInventory player inv)))

(defn menu-entry
  [& {:keys [item permission action]
      :as   entry-map}]
  entry-map)