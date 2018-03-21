(ns sponge-clj.lambda-mobs
  (:require [sponge-clj.entity :as e]
            [sponge-clj.database :as db]
            [sponge-clj.world :as w]
            [sponge-clj.items :as i]
            [sponge-clj.events :as ev]
            [clojure.tools.trace :refer :all]
            [sponge-clj.cause :as c])
  (:import (org.spongepowered.api.world World)
           (org.spongepowered.api.entity Entity)
           (org.spongepowered.api.entity.living Living)
           (org.spongepowered.api.entity.living.player Player)
           (org.spongepowered.api.event.entity DestructEntityEvent$Death DamageEntityEvent)
           (org.spongepowered.api.event.item.inventory DropItemEvent$Destruct)))


(def ^:private mobs (atom {}))

(def ^:private recently-dead (atom {}))

(defn register
  [id mob]
  (swap! mobs assoc id mob))

(defn get-mob
  "Returns lambda-mob if exists, else nil"
  [key]
  (if (contains? @mobs key)
    (get @mobs key)
    nil))

(defn mark-lambda-mob
  [^Entity entity mob]
  (let [uuid (-> entity
                 (.getUniqueId)
                 (.toString))]
    (do (db/assoc-in :lambda-mobs [(keyword uuid)] (:id mob))
        entity)))

(defn unmark-lambda-mob
  [^Entity entity]
  (let [uuid (-> entity
                 (.getUniqueId)
                 (.toString))]
    (do (db/dissoc :lambda-mobs (keyword uuid))
        entity)))

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
             true (e/set-persistent true)
             (contains? mob :display-name) (e/set-display-name (:display-name mob))
             (contains? mob :health) (e/set-max-health (:health mob))
             (contains? mob :speed) (e/set-speed (:speed mob))
             (contains? mob :damage) (e/set-damage (:damage mob))
             (contains? mob :equipment) (e/set-equipment (:equipment mob))
             true (e/spawn world)
             ))))

(defn mark-as-recently-dead
  [id ^Entity entity]
  (swap! recently-dead assoc (keyword (str (.getUniqueId entity))) id))

(defn process-entity-death
  [event ^Living entity]
  (if (not (isa? Player entity))
    (when-let [id (db/get-in :lambda-mobs [(keyword (str (.getUniqueId entity)))])]
      (let [mob (get-mob id)]
        (do (mark-as-recently-dead id entity)
            (unmark-lambda-mob entity))))
    nil))

(defn process-items-drop
  [event, ^Entity entity]
  (if (not (isa? Player entity))
    (when-let [id (get @recently-dead (keyword (str (.getUniqueId entity))))]
      (let [mob   (get-mob id)
            loc   (e/get-loc entity)
            items (get mob :drop)]
        (do
          (doseq [i items]
              (i/spawn-item loc i))
          (swap! recently-dead dissoc id)
          (.setCancelled (:event event) true)
          )))
    nil))

(defn process-entity-damage
  [^DamageEntityEvent event, ^Entity entity]
  (if (not (isa? Player entity))
    (when-let [id (db/get-in :lambda-mobs [(keyword (str (.getUniqueId entity)))])]
      (let [mob              (get-mob id)
            raw-event        (:event event)
            base-damage      (:base-damage event)
            damage-type      (:damage-type event)
            damage-modifiers (:damage-modifiers mob)
            new-damage       (cond (contains? damage-modifiers damage-type)
                                   (* (get damage-modifiers damage-type) base-damage)
                                   :else base-damage)]
        (do (.setBaseDamage raw-event new-damage))))
    nil))

(defn def-mob
  [& {:keys [id entity-type] :as mob}]
  {:pre [(some? id)
         (some? entity-type)]}
  (register id mob))

(ev/register-listener
  DestructEntityEvent$Death
  (fn [event]
    (process-entity-death event (:entity event))))

(ev/register-listener
  DropItemEvent$Destruct
  (fn [event]
    (when-let [entity (c/first-in (:cause event) Entity)]
      (process-items-drop event entity))
    ))

(ev/register-listener
  DamageEntityEvent
  (fn [event]
    (process-entity-damage event (:entity event))))