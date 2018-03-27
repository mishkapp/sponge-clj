(ns test
  (:use
    [sponge-clj.util]
    [sponge-clj.items]
    [sponge-clj.potion-effects]
    [sponge-clj.time]
    [sponge-clj.logger]
    [sponge-clj.triggers]
    [sponge-clj.world]
    [sponge-clj.lambda-items]
    [sponge-clj.lambda-mobs]
    [sponge-clj.sponge]
    [sponge-clj.events]
    [sponge-clj.random]
    [sponge-clj.enchantments]
    )
  (:import (org.spongepowered.api.event.entity MoveEntityEvent)
           (org.spongepowered.api.entity.living.player Player)
           (org.spongepowered.api.event.network ClientConnectionEvent$Join)))

(defn sword-use
  "Executed on sword use"
  [player item]
  (do
    (send-message player "&bHa-ha-ha, it's cursed!")
    (add-effect player (effect "minecraft:slowness" 10 100))))

(def-item
  :id :skeleton-king-sword
  :material "minecraft:diamond_sword"
  :display-name "&3Greatsword of the Skeleton King"
  :lore [
         "&6A powerful sword used by"
         "&6the King of Skeletons."
         ]
  :enchantments [
                 (enchantment "minecraft:sharpness" 2)
                 (enchantment "minecraft:knockback" 2)
                 (enchantment "minecraft:fire_aspect" 3)
                 ]
  :action-delay (seconds 1.5)

  :action-fn sword-use)

(def-mob
  :id :skeleton-king
  :entity-type "minecraft:skeleton"
  :display-name "&6&lSkeleton king"
  :health 4
  :damage 4                                                 ;??
  :speed 0.2
  ;:armor            2
  :damage-modifiers {
                     :attack     2.5
                     :fire       -1
                     :projectile 0.1
                     :magma      -1
                     }
  :drop [
         `(item-stack "minecraft:diamond_block" (from-range 5 10))
         `(cond (chance 1/3) (item-stack "minecraft:apple" 1))
         (lambda-item-stack :skeleton-king-sword)
         ]
  :equipment {
              :head      (item-stack "minecraft:gold_block")
              :main-hand (lambda-item-stack :skeleton-king-sword)
              :off-hand  (item-stack "minecraft:shield")
              }
  )

;(def-cmd
;  {
;   :executor test-fn
;   :permission  "testcmd.exec"
;   :arguments [
;               (string-arg "id")
;               (player-arg "player")
;               ]
;   :description "Just test command"
;   })
;
;(def-cmd-child
;  {
;   :permission "testcmd.exec.child"
;   :arguments [(string-arg "id")]
;   :description "Just a child command"
;   })

(def-trigger
  :id :my-block
  :event-type MoveEntityEvent
  :predicate (fn [event]
               (let [location-to (:location-to event)
                     entity      (:entity event)]
                 (and (isa? Player entity)
                      (block-location-equals? location-to (location "world" 32 65 310)))))
  :action (fn [event]
            (let [entity (:entity event)]
              (send-message entity "Hue hue")))
  :delay (seconds 1)

  ;(def-walk-trigger
  ;   :predicate  (fn [{:keys                 [event player block]
  ;                     {:keys [world x y z]} :location}]
  ;                 (and (= x 100)
  ;                      (= y 60)
  ;                      (= z 100)))
  ;   :action     (fn [{:keys                 [event player block]
  ;                     {:keys [world x y z]} :location}]
  ;                 (teleport-to player (location "world" 200 60 200)))
  ;   :delay      (seconds 1)
  ;   :permission "testtrigger.perm")

  ;(def-walktp-trigger
  ;  {
  ;   :from (location "world" 60 90 90)
  ;   :to   (location "world" 200 60 200)
  ;   })

  (comment
    (sponge-clj.sponge/>>sponge #(spawn-mob :skeleton-king {:world "world" :x 35 :y 64 :z 308}))

    (register-listener ClientConnectionEvent$Join (fn [event]
                                                    (let [^ClientConnectionEvent$Join eve (:event event)
                                                          player                          (-> eve
                                                                                              (.getCause)
                                                                                              (.root))]
                                                      (send-message player "Ho-hoho"))))
    ))
