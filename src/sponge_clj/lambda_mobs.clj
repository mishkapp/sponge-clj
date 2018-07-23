(ns sponge-clj.lambda-mobs
  (:require [sponge-clj.entity :as e]
            [sponge-clj.world :as w]
            [sponge-clj.items :as i]
            [sponge-clj.triggers :as t]
            [clojure.tools.trace :refer :all]
            [sponge-clj.random :as rnd]
            [sponge-clj.cause :as c]
            [sponge-clj.sponge :as sp])
  (:import (org.spongepowered.api.world World)
           (org.spongepowered.api.entity Entity)
           (org.spongepowered.api.event.entity DamageEntityEvent SpawnEntityEvent)
           (org.spongepowered.api.event.item.inventory DropItemEvent$Destruct)
           (org.spongepowered.api.event.cause EventContextKeys)
           (sponge_clj LambdaMobData)))

(def ^:private mobs (atom {}))

(def ^:private spawns (atom {}))

(defn- register
  [id mob]
  (swap! mobs assoc id mob))

(defn get-mob
  "Returns lambda-mob if exists, else nil"
  [key]
  (if (contains? @mobs key)
    (get @mobs key)
    nil))

(defn lambda-mob?
  [^Entity entity]
  (if (nil? entity)
    false
    (-> entity
        (.get ^Class LambdaMobData)
        (.isPresent))))

(defn mark-lambda-mob
  [^Entity entity mob]
  (.offer entity (.get (.getOrCreate entity LambdaMobData)))
  (.offer entity (:lambda-mob-id @sponge-clj.keys/sponge-keys) (:id mob))
  entity)

(defn unmark-lambda-mob
  [^Entity entity]
  (.remove entity LambdaMobData)
  entity)

(defn lambda-mob-type
  [^Entity entity]
  (-> entity
      (.get (:lambda-mob-id @sponge-clj.keys/sponge-keys))
      (.orElse nil)))

(defn spawn-mob
  (^Entity [id loc]
   {:pre [(contains? @mobs id)
          (some? loc)]}
   (let [mob          (get @mobs id)
         mob-type     (e/entity-type (:entity-type mob))
         ^World world (w/get-world loc)]
     (cond-> (w/as-sponge-location loc)
             true (.createEntity mob-type)
             true (mark-lambda-mob mob)
             (contains? mob :persistent) (e/set-persistent (:persistent mob))
             (contains? mob :display-name) (e/set-display-name (eval (:display-name mob)))
             (contains? mob :health) (e/set-max-health (eval (:health mob)))
             (contains? mob :speed) (e/set-speed (eval (:speed mob)))
             (contains? mob :damage) (e/set-damage (eval (:damage mob)))
             (contains? mob :equipment) (e/set-equipment (:equipment mob))
             true (e/spawn world)
             ))))

(defn process-items-drop
  [event]
  (when-let [entity (c/first-in (:cause event) Entity)]
    (when-let [id (lambda-mob-type entity)]
      (let [mob   (get-mob id)
            loc   (e/get-loc entity)
            items (get mob :drop)]
        (do
          (doseq [it items]
            (when-let [item (eval it)]
              (i/spawn-item loc item)))
          (.setCancelled (:event event) true)
          (unmark-lambda-mob entity)
          )))))

(defn process-entity-damage
  [^DamageEntityEvent event, ^Entity entity]
    (let [mob              (lambda-mob-type entity)
          raw-event        (:event event)
          base-damage      (:base-damage event)
          damage-type      (:damage-type event)
          damage-modifiers (:damage-modifiers mob)
          new-damage       (if (contains? damage-modifiers damage-type)
                             (* (get damage-modifiers damage-type) base-damage)
                             base-damage)]
      (.setBaseDamage raw-event new-damage)))

(defn- cause?
  [cause spawn]
  (let [spawn-causes (:causes spawn)]
    (or (nil? spawn-causes)
        (.contains (:causes spawn) cause))))

(defn- entity?
  [entity spawn]
  (let [spawn-entities (:entity-types spawn)]
    (or (nil? spawn-entities)
        (.contains spawn-entities entity))))

(defn- world?
  [location spawn]
  (let [spawn-worlds (:worlds spawn)
        world        (w/get-world-name location)]
    (or (nil? spawn-worlds)
        (.contains spawn-worlds world))))

(defn- biome?
  [location spawn]
  (let [spawn-biomes (:biomes spawn)
        biome        (w/get-biome location)
        biome-name   (-> biome
                         (.getId))]
    (or (nil? spawn-biomes)
        (.contains spawn-biomes biome-name))))

(defn- block?
  [location spawn]
  (let [spawn-blocks (:blocks spawn)
        location     (update-in location [:y] - 1)
        block        (w/get-block location)
        block-type   (-> block
                         (.getType)
                         (.getName))]
    (or (nil? spawn-blocks)
        (.contains spawn-blocks block-type))))

(defn- predicate?
  [spawn event]
  (or (nil? (:predicate spawn))
      (apply (:predicate) [event])))

(defn- process-entity-spawn
  [^Entity entity location cause event]
  (when-let [cause (-> cause
                       (.getContext)
                       (.get (EventContextKeys/SPAWN_TYPE))
                       (.orElse nil))]
    (let [entity-type (e/get-type entity)
          cause-type  (-> cause
                          (.getId))
          spawns      (vec (vals @spawns))
          spawns      (filter (every-pred #(cause? cause-type %)
                                          #(entity? entity-type %)
                                          #(world? location %)
                                          #(biome? location %)
                                          #(block? location %)
                                          #(predicate? % event)
                                          )
                              spawns)
          spawns      (sort-by :priority spawns)
          spawns      (filter #(rnd/chance (:chance %)) spawns)]
      (when-let [res-spawn (first spawns)]
        (do
          (cond (= :replace (:type res-spawn)) (do
                                                 (.setCancelled (:event event) true)
                                                 (-> entity
                                                       (.remove))))
            (sp/>>sponge #(spawn-mob (:mob res-spawn) location))
            )))))

(defn- process-entities-for-spawn
  [event]
  (let [entities (:entities event)
        cause    (:cause event)]
    (dorun (map #(process-entity-spawn % (e/get-loc %) cause event) entities))))

(defn def-mob
  [& {:keys [id entity-type] :as mob}]
  {:pre [(some? id)
         (some? entity-type)]}
  (register id mob))

(defn register-spawn
  [& {:keys [id mob worlds biomes entity-types chance priority blocks causes type raw-predicate]
      :or   {type     :replace
             chance   1.0
             priority 1
             causes   [:chunk-load]}}]
  {:pre [(some? id)
         (some? mob)]}
  ;probably there is more convenient way to do this
  (let [spawn {:mob           mob
               :worlds        worlds
               :biomes        biomes
               :entity-types  entity-types
               :chance        chance
               :priority      priority
               :blocks        blocks
               :causes        causes
               :type          type
               :raw-predicate raw-predicate}]
    (swap! spawns assoc id spawn)))

(t/def-trigger
  :id :lambda-mob-item-drop
  :event-type DropItemEvent$Destruct
  :predicate #(lambda-mob? (c/first-in (:cause %) Entity))
  :action #(process-items-drop %)
  :delay 0
  )

(t/def-trigger
  :id :lambda-mob-damage
  :event-type DamageEntityEvent
  :predicate #(lambda-mob? (:entity %))
  :action #(process-entity-damage % (:entity %))
  :delay 0
  )

(t/def-trigger
  :id :lambda-mobs-spawn
  :event-type SpawnEntityEvent
  :predicate #(not (lambda-mob? (:entity %)))
  :action #(process-entities-for-spawn %)
  :delay 0
  )

;(t/def-trigger
;  :id :lambda-mob-ticker
;  :event-type TargetEntityEvent
;  :predicate #(isa? Skeleton (:entity %))
;  :action #(println (:entity %))
;  :delay 0)
;todo: remove entity from registry when server despawn it (not death)