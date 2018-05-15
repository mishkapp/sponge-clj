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
    [clojure.reflect]
    ))

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
  ;todo: add passenger
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

(register-spawn
  :id :skeleton-king-overworld-spawner
  :mob :skeleton-king
  :worlds ["world"]
  :biomes ["plains"]
  :entity-types ["minecraft:zombie" "minecraft:skeleton"]
  :chance 1/3
  :priority 1
  :blocks ["minecraft:grass"]
  ; Possible causes
  ; sponge:block_spawning sponge:breeding sponge:chunk_load sponge:custom
  ; sponge:dispense sponge:dropped_item sponge:experience sponge:falling_block
  ; sponge:mob_spawner sponge:passive sponge:placement sponge:plugin sponge:projectile
  ; sponge:spawn_egg sponge:structure sponge:tnt_ignite sponge:weather sponge:world_spawner
  :causes ["sponge:world_spawner"]
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
  :predicate (fn [event]
               (let [location-to (:location-to event)
                     entity      (:entity event)]
                 (and (player? entity)
                      (block-location-equals? location-to (location "world" 35 65 310)))))
  :action (fn [event]
            (let [entity (:entity event)]
              (send-message entity "Hue hue")))
  :delay (seconds 1)
  )

(def test-menu-entry
  (menu-entry
    :item (item-stack "minecraft:apple")
    :permission "testmenu.test"
    :action #(send-message % "Huuuuray!")))

(def-menu main-menu
  :title (text :gold "Test menu")
  :lines 6
  :content {
            [2 2] test-menu-entry
            [2 3] test-menu-entry
            [2 4] test-menu-entry
            }
  )

(def-cmd
  :aliases ["testmenu"]
  :permission "testmenu.open"
  :executor (fn [src args] (open-menu main-menu src)))