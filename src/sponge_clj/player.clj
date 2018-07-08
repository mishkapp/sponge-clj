(ns sponge-clj.player
  (:import (org.spongepowered.api.entity.living.player Player)
           (org.spongepowered.api.item.inventory ItemStack)
           (java.util UUID)
           (org.spongepowered.api Sponge)))

(defn give-item
  [^Player player ^ItemStack is]
  {:pre [(some? player) (some? is)]}
  (-> player
      (.getInventory)
      (.offer is))
  player)

(defn has-permission
  [^Player player perm]
  (-> player
      (.hasPermission perm)))

(defn is-player?
  [obj]
  (isa? Player obj))

(defn player-name
  [^Player player]
  (-> player
      (.getName)))

(defn player-uuid
  [^Player player]
  (-> player
      (.getUniqueId)))

(defn get-player-by-name
  [name]
  (-> (Sponge/getServer)
      (.getPlayer ^String name)
      (.orElse nil)))

(defn get-player-by-uuid
  [uuid]
  (let [uuid (if (isa? UUID uuid) uuid (UUID/fromString (str uuid)))]
    (-> (Sponge/getServer)
        (.getPlayer ^UUID uuid)
        (.orElse nil))))
