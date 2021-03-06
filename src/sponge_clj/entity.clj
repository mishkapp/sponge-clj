(ns sponge-clj.entity
  (:require [sponge-clj.world :as w])
  (:use [sponge-clj.sponge]
        [sponge-clj.text]
        [clojure.tools.trace])
  (:import (org.spongepowered.api.entity EntityType Entity ArmorEquipable)
           (org.spongepowered.api.data.key Keys)
           (org.spongepowered.api.data.type HandTypes)
           (org.spongepowered.api.world World)))

(defn entity-type
  "Returns entity type for given id, or nil if there is no item with this id"
  [id]
  {:pre [(some? id)]}
  (get-catalog-type EntityType id))

(defn get-entity-uuid
  [^Entity entity]
  (if (nil? entity)
    nil
    (.getUniqueId entity)))

(defn set-display-name
  [^Entity entity, display-name]
  {:pre [(some? entity)
         (some? display-name)]}
  (.offer entity Keys/DISPLAY_NAME (text display-name))
  entity)

(defn set-max-health
  [^Entity entity, health]
  {:pre [(some? entity)
         (some? health)]}
  (.offer entity Keys/MAX_HEALTH (double health))
  (.offer entity Keys/HEALTH (double health))
  entity)

(defn set-damage
  [^Entity entity, damage]
  {:pre [(some? entity)
         (some? damage)]}
  (.offer entity Keys/ATTACK_DAMAGE damage)
  entity)

(defn set-persistent
  [^Entity entity, value]
  {:pre [(some? entity)
         (some? value)]}
  (.offer entity Keys/PERSISTS value)
  entity)

(defn set-speed
  [^Entity entity, value]
  {:pre [(some? entity)
         (some? value)]}
  (.offer entity Keys/WALKING_SPEED value)
  entity)

(defn spawn
  [^Entity entity ^World world]
  (-> world
      (.spawnEntity entity))
  entity)

(defn get-loc
  [^Entity entity]
  (-> entity
      (.getLocation)
      (w/location)))

(defn get-type
  [^Entity entity]
  (-> entity
      (.getType)
      (.getId)))

(defn add-passenger
  [^Entity tr ^Entity pass]
  (-> tr
      (.addPassenger pass))
  tr)

(defn set-equipment
  [^Entity entity, equipment]
  {:pre [(some? entity)
         (some? equipment)]}
  (if (instance? ArmorEquipable entity)
    (let [^ArmorEquipable ae-entity entity]
      (do (.setHelmet ae-entity (eval (:head equipment)))
          (.setChestplate ae-entity (eval (:chestplate equipment)))
          (.setLeggings ae-entity (eval (:leggings equipment)))
          (.setBoots ae-entity (eval (:boots equipment)))
          (.setItemInHand ae-entity (HandTypes/MAIN_HAND) (eval (:main-hand equipment)))
          (.setItemInHand ae-entity (HandTypes/OFF_HAND) (eval (:off-hand equipment)))
          ae-entity)
      entity)))

(defn get-nearest-entity
  [loc range predicate]
  (let [world (w/get-world loc)
        entities (-> world
                 (.getNearbyEntities (w/loc->vec3d loc) range))]
    (filter predicate entities)
    ))