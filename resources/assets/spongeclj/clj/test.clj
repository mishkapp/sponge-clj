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
    [sponge-clj.commands]
    [sponge-clj.text]
    [sponge-clj.menu]
    [sponge-clj.particles])
  (:import (org.spongepowered.api.entity Entity)))

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
  :id :king-chicken
  :entity-type "minecraft:chicken"
  :display-name (text :gold "Sanders' treasure")
  :health 50
  :speed 2
  :drop [
         `(cond (chance 1/50) (item-stack "minecraft:golden_apple" 1))
         ])

(def-mob
  :id :skeleton-king
  :entity-type "minecraft:skeleton"
  :display-name (text :red :bold "Skeleton king")
  :health 4
  :damage 4                                                 ;??
  :speed 0.2
  ;:armor            2
  :passenger `(cond (chance 1/10) :king-chicken)
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

(def-mob
  :id :skeleton-king2
  :entity-type "minecraft:skeleton"
  :display-name (text :red :bold "Skeleton king")
  :health 4
  :damage 4                                                 ;??
  :speed 0.2
  ;:armor            2
  :passenger `(cond (chance 1/10) :king-chicken)
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
              :head      (item-stack "minecraft:iron_block")
              :main-hand (lambda-item-stack :skeleton-king-sword)
              :off-hand  (item-stack "minecraft:shield")
              }
  )

(def-mob
  :id :skeleton-king3
  :entity-type "minecraft:skeleton"
  :display-name (text :red :bold "Skeleton king")
  :health 4
  :damage 4                                                 ;??
  :speed 0.2
  ;:armor            2
  :passenger `(cond (chance 1/10) :king-chicken)
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
              :head      (item-stack "minecraft:diamond_block")
              :main-hand (lambda-item-stack :skeleton-king-sword)
              :off-hand  (item-stack "minecraft:shield")
              }
  )

(def-mob
  :id :stacker-skeleton
  :entity-type "minecraft:chicken"
  :display-name (text :dark-green "stacker-skeleton")
  :health 4
  :passenger `(cond (chance 1/2) :stacker-skeleton)
  :drop [
         `(item-stack "minecraft:bone" (from-range 5 10))
         ]
  )

(def-mob
  :id :skilled-skeleton
  :entity-type "minecraft:skeleton"
  :display-name (text :dark-purple :bold "SKILLED")
  :health 10
  :equipment {
              :head (item-stack "minecraft:cyan_stained_glass")
              }
  :skills [
           {
            :trigger   :tick
            :target-fn (fn [src]
                         "")
            :skill-fn  (fn [src targets args]
                         "")
            :cooldown  (ticks 100)
            }
           ]
  )

(register-spawn
  :id :skilled-skeleton-spawner
  :mob :stacker-skeleton
  ;:biomes ["plain"]
  :entity-types ["minecraft:skeleton"]
  :chance 1
  :priority 1
  ;:blocks ["minecraft:grass"]
  ; Possible causes
  ; sponge:block_spawning sponge:breeding sponge:chunk_load sponge:custom
  ; sponge:dispense sponge:dropped_item sponge:experience sponge:falling_block
  ; sponge:mob_spawner sponge:passive sponge:placement sponge:plugin sponge:projectile
  ; sponge:spawn_egg sponge:structure sponge:tnt_ignite sponge:weather sponge:world_spawner
  :causes ["sponge:spawn_egg"]
  :type :replace
  ;raw predicate that handles destructured ConstructEntityEvent$Pre and decides can mob be spawned or not
  ;:raw-predicate predicate-fn
  )
(def cmd-a (cmd
             :executor #(do (println %1)
                            (println (:id %2)))
             :permission "testcmd.exec.child.a"
             :arguments [(string-arg "id")]
             :description "Just a child command A"))

(def cmd-b (cmd
             :executor (fn [src, args]
                         (do (println args)
                             (send-message src "cmd-b")))
             :permission "testcmd.exec.child.b"
             :arguments [(string-arg "id")]
             :description "Just a child command B"))

(def-cmd
  :aliases ["test" "tst"]
  ;:executor test-fn
  :permission "testcmd.exec"
  :children {["a"]     cmd-a
             ["b" "v"] cmd-b}
  :description "Just test command"
  :extended-description "And it's extended description")



(def-trigger
  :id :my-block
  :event-type :walk
  :predicate #(let [location-to (:location-to %)
                    entity (:entity %)]
                (and (player? entity)
                     (block-location-equals? location-to (location "world" 423 72 -63))))
  :action (fn [event]
            (let [^Entity entity (:entity event)
                  loc (location (.getLocation entity))
                  pe (particle :redstone-dust {:color (color 0 255 0)})]
              (spawn-particle pe loc)))
  :delay (seconds 1)
  )

(def test-menu-entry
  (menu-entry
    :item (item-stack "minecraft:apple")
    :permission "testmenu.test"
    :action #(send-message % "Huuuuray!")))

(def-menu main-menu
          :title (text :gold "Test menu")
          :rows 6
          :content {
                    [1 1] test-menu-entry
                    [2 2] test-menu-entry
                    [2 3] test-menu-entry
                    [2 4] test-menu-entry
                    }
          )

(def-cmd
  :aliases ["testmenu"]
  :permission "testmenu.open"
  :executor (fn [src args] (open-menu main-menu src)))