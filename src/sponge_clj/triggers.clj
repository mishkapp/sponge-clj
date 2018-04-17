(ns sponge-clj.triggers
  (:require [sponge-clj.events :as ev]
            [sponge-clj.time :refer :all])
  (:import (org.spongepowered.api.event Event)
           (org.spongepowered.api.event.entity MoveEntityEvent AffectEntityEvent AttackEntityEvent BreedEntityEvent ChangeEntityEquipmentEvent ChangeEntityExperienceEvent ChangeEntityPotionEffectEvent CollideEntityEvent DamageEntityEvent DestructEntityEvent DestructEntityEvent$Death HealEntityEvent IgniteEntityEvent LeashEntityEvent MoveEntityEvent$Teleport RideEntityEvent$Mount RideEntityEvent$Dismount TameEntityEvent)
           (java.util UUID)
           (org.spongepowered.api.event.action CollideEvent FishingEvent LightningEvent SleepingEvent)
           (org.spongepowered.api.event.block.tileentity BrewingEvent ChangeSignEvent SmeltEvent)
           (org.spongepowered.api.event.block ChangeBlockEvent CollideBlockEvent InteractBlockEvent TickBlockEvent)
           (org.spongepowered.api.event.economy EconomyTransactionEvent)
           (org.spongepowered.api.event.entity.ai AITaskEvent SetAITargetEvent)
           (org.spongepowered.api.event.entity.explosive DefuseExplosiveEvent DetonateExplosiveEvent PrimeExplosiveEvent)
           (org.spongepowered.api.event.entity.living.humanoid.player CooldownEvent KickPlayerEvent RespawnPlayerEvent)
           (org.spongepowered.api.event.entity.living.humanoid ChangeGameModeEvent)
           (org.spongepowered.api.event.entity.projectile LaunchProjectileEvent)
           (org.spongepowered.api.event.message MessageEvent)
           (org.spongepowered.api.event.network.rcon RconConnectionEvent)
           (org.spongepowered.api.event.world ExplosionEvent)))

(def ^:private triggers (atom {}))
(def ^:private last-uses (atom {}))

(def event-map {
                :collide            CollideEvent
                :fishing            FishingEvent
                :lightning          LightningEvent
                :sleep              SleepingEvent
                :brew               BrewingEvent
                :sign-edit          ChangeSignEvent
                :smelt              SmeltEvent
                :change-block       ChangeBlockEvent
                :collide-block      CollideBlockEvent
                :interact-block     InteractBlockEvent
                :tick-block         TickBlockEvent
                :economy            EconomyTransactionEvent
                :ai-task            AITaskEvent
                :set-ai-target      SetAITargetEvent
                :explosive-defuse   DefuseExplosiveEvent
                :explosive-detonate DetonateExplosiveEvent
                :explosive-prime    PrimeExplosiveEvent
                :item-cooldown      CooldownEvent
                :player-kick        KickPlayerEvent
                :player-respawn     RespawnPlayerEvent
                :game-mode          ChangeGameModeEvent
                :projectile-launch  LaunchProjectileEvent
                ;entity events
                :entity-affect      AffectEntityEvent
                :entity-attacked    AttackEntityEvent
                :entity-breed       BreedEntityEvent
                :entity-equipment   ChangeEntityEquipmentEvent
                :entity-experience  ChangeEntityExperienceEvent
                :entity-potion      ChangeEntityPotionEffectEvent
                :entity-collide     CollideEntityEvent
                :entity-damage      DamageEntityEvent
                :entity-destruct    DestructEntityEvent
                :entity-death       DestructEntityEvent$Death
                :entity-heal        HealEntityEvent
                :entity-ignite      IgniteEntityEvent
                :entity-leash       LeashEntityEvent
                :entity-move        MoveEntityEvent
                :walk               MoveEntityEvent
                :entity-teleport    MoveEntityEvent$Teleport
                :entity-mount       RideEntityEvent$Mount
                :entity-dismount    RideEntityEvent$Dismount
                :entity-tame        TameEntityEvent

                :message            MessageEvent
                :rcon               RconConnectionEvent
                :explosion          ExplosionEvent
                })

(defn dispatch-event
  [^Event event]
  (let [type-pred   #(instance? % (:event event))
        filter-pred #(and (some type-pred (:event-type %))
                          (if (and (some? (:predicate %)) (fn? (:predicate %))) (apply (:predicate %) [event]) true)
                          (>= (- (System/currentTimeMillis) (:delay %)) (get @last-uses (:id %) 0)))
        triggers    (filter filter-pred (vals @triggers))]
    (doseq [tr triggers]
      (swap! last-uses assoc (:id tr) (System/currentTimeMillis))
      (apply (:action tr) [event]))))

(defn- prepare-type
  [type]
  {:pre [(or (keyword? type)
             (class? type))]}
  (if (keyword? type)
    (get event-map type)
    type))

(defn- prepare-type-vector
  [type-vector]
  (map prepare-type type-vector))

(defn- prepare-event-type
  [event-type]
  (if (vector? event-type)
    (prepare-type-vector event-type)
    (prepare-event-type (vector event-type))))

(defn def-trigger
  [& {:keys [id event-type predicate action delay]
      :or   {delay (seconds 1)
             id    (keyword (str (UUID/randomUUID)))}
      :as   trigger}]
  {:pre [(some? id)
         (keyword? id)
         (some? event-type)
         (some? action)
         (fn? action)]}
  (swap! last-uses assoc id (System/currentTimeMillis))
  (swap! triggers assoc id (assoc trigger :event-type (prepare-event-type event-type))))

(defn def-walk-trigger
  [& {:as trigger}]
  (apply def-trigger (assoc trigger :event MoveEntityEvent)))

(defn init
  []
  (ev/register-listener Event
                        (fn [event]
                          (dispatch-event event))))