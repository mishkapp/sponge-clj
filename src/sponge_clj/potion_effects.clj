(ns sponge-clj.potion-effects
  (:import (org.spongepowered.api.data.key Keys)
           (org.spongepowered.api.entity Entity)
           (org.spongepowered.api.effect.potion PotionEffect PotionEffectType)
           (org.spongepowered.api Sponge)))

(defn add-effect
  "Adds effect to entity"
  [^Entity entity, ^PotionEffect effect]
  {:pre [(some? entity), (some? effect)]}
  (let [effects (-> (.get entity Keys/POTION_EFFECTS)
                    (.orElse []))]
    (do (.offer entity Keys/POTION_EFFECTS (concat effects [effect]))
        entity)))

(defn effect-type
  "Retrieves potion type by id"
  [id]
  (-> (Sponge/getGame)
      (.getRegistry)
      (.getType PotionEffectType (str id))
      (.orElse nil)))

(defn effect
  "Creates potion effect"
  [id level duration]
  (when-let [pe-type (effect-type id)]
    (-> (PotionEffect/builder)
        (.potionType pe-type)
        (.amplifier level)
        (.duration duration)
        (.build))))