(ns sponge-clj.particles
  (:require [sponge-clj.world :as w])
  (:import (org.spongepowered.api.effect.particle ParticleTypes ParticleEffect ParticleOptions ParticleEffect$Builder)
           (org.spongepowered.api.util Color)))

(def particles
  {
   :ambient-mob-spell    ParticleTypes/AMBIENT_MOB_SPELL
   :angry-villager       ParticleTypes/ANGRY_VILLAGER
   :barrier              ParticleTypes/BARRIER
   :block-crack          ParticleTypes/BLOCK_CRACK
   :block-dust           ParticleTypes/BLOCK_DUST
   :break-block          ParticleTypes/BREAK_BLOCK          ;??
   :cloud                ParticleTypes/CLOUD
   :critical-hit         ParticleTypes/CRITICAL_HIT
   :damage-indicator     ParticleTypes/DAMAGE_INDICATOR
   :dragon-breath        ParticleTypes/DRAGON_BREATH
   :dragon-breath-attack ParticleTypes/DRAGON_BREATH_ATTACK
   :drip-lava            ParticleTypes/DRIP_LAVA
   :drip-water           ParticleTypes/DRIP_WATER
   :enchanting-glyphs    ParticleTypes/ENCHANTING_GLYPHS
   :ender-teleport       ParticleTypes/ENDER_TELEPORT
   :end-rod              ParticleTypes/END_ROD
   :explosion            ParticleTypes/EXPLOSION
   :falling-dust         ParticleTypes/FALLING_DUST
   :fertilizer           ParticleTypes/FERTILIZER
   :fireworks            ParticleTypes/FIREWORKS
   :fireworks-spark      ParticleTypes/FIREWORKS_SPARK
   :fire-smoke           ParticleTypes/FIRE_SMOKE
   :flame                ParticleTypes/FLAME
   :footstep             ParticleTypes/FOOTSTEP
   :guardian-appearance  ParticleTypes/GUARDIAN_APPEARANCE
   :happy-villager       ParticleTypes/HAPPY_VILLAGER
   :heart                ParticleTypes/HEART
   :huge-explosion       ParticleTypes/HUGE_EXPLOSION
   :instant-spell        ParticleTypes/INSTANT_SPELL
   :item-crack           ParticleTypes/ITEM_CRACK
   :large-explosion      ParticleTypes/LARGE_EXPLOSION
   :large-smoke          ParticleTypes/LARGE_SMOKE
   :lava                 ParticleTypes/LAVA
   :magic-critical-hit   ParticleTypes/MAGIC_CRITICAL_HIT
   :mobspawner-flames    ParticleTypes/MOBSPAWNER_FLAMES
   :mob-spell            ParticleTypes/MOB_SPELL
   :note                 ParticleTypes/NOTE
   :portal               ParticleTypes/PORTAL
   :redstone-dust        ParticleTypes/REDSTONE_DUST
   :slime                ParticleTypes/SLIME
   :smoke                ParticleTypes/SMOKE
   :snowball             ParticleTypes/SNOWBALL
   :snow-shovel          ParticleTypes/SNOW_SHOVEL
   :spell                ParticleTypes/SPELL
   :splash-potion        ParticleTypes/SPLASH_POTION
   :suspended            ParticleTypes/SUSPENDED
   :suspended-depth      ParticleTypes/SUSPENDED_DEPTH
   :sweep-attack         ParticleTypes/SWEEP_ATTACK
   :town-aura            ParticleTypes/TOWN_AURA
   :water-bubble         ParticleTypes/WATER_BUBBLE
   :water-splash         ParticleTypes/WATER_SPLASH
   :water-wake           ParticleTypes/WATER_WAKE
   :witch-spell          ParticleTypes/WITCH_SPELL
   })

(def particle-options
  {
   :block-state              ParticleOptions/BLOCK_STATE
   :color                    ParticleOptions/COLOR
   :direction                ParticleOptions/DIRECTION
   :firework-effects         ParticleOptions/FIREWORK_EFFECTS
   :item-stack-snapshot      ParticleOptions/ITEM_STACK_SNAPSHOT
   :note                     ParticleOptions/NOTE
   :offset                   ParticleOptions/OFFSET
   :potion-effect-type       ParticleOptions/POTION_EFFECT_TYPE
   :quantity                 ParticleOptions/QUANTITY
   :scale                    ParticleOptions/SCALE
   :slow-horizontal-velocity ParticleOptions/SLOW_HORIZONTAL_VELOCITY
   :velocity                 ParticleOptions/VELOCITY})

(defn color
  [r g b]
  (Color/ofRgb r g b))

(defn- apply-opts
  [^ParticleEffect$Builder builder opts]
  (doseq [k (keys opts)]
    (.option builder (get particle-options k) (get opts k)))
  builder)

(defn particle
  ([id]
   {:pre (contains? particles id)}
   (particle id {}))
  ([id opts]
   {:pre (contains? particles id)}
   (let [pt (get particles id)]
     (-> (ParticleEffect/builder)
         (.type pt)
         (.quantity 1)
         (apply-opts opts)
         (.build)))))

(defn spawn-particle
  [p loc]
  (-> (w/get-world loc)
      (.spawnParticles p (w/loc->vec3d loc))))