(ns sponge-clj.player
  (:import (org.spongepowered.api.entity.living.player Player)
           (org.spongepowered.api.item.inventory ItemStack)))

(defn give-item
  [^Player player ^ItemStack is]
  {:pre [(some? player) (some? is)]}
  (-> player
      (.getInventory)
      (.offer is))
  player)
