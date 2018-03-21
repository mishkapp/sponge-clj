(ns sponge-clj.events
  (:require [sponge-clj.sponge :as sp]
            [sponge-clj.cause :as c])
  (:use [sponge-clj.world])
  (:import (org.spongepowered.api.event.entity MoveEntityEvent TargetEntityEvent DamageEntityEvent)
           (org.spongepowered.api.event Event EventListener Order)
           (org.spongepowered.api Sponge)
           (org.spongepowered.api.event.block InteractBlockEvent)
           (org.spongepowered.api.event.cause.entity.damage.source DamageSource)))

(defn as-damage-type
  [raw-type]
  (keyword (.getId raw-type)))

(defprotocol EventDestructurizer
  (destructure-event [event]
    "Destructure event to map with widely use keys,
    event itself (as java object) can be obtained with :event key"))

(extend-protocol EventDestructurizer
  MoveEntityEvent
  (destructure-event [^MoveEntityEvent event]
    (let [result        {:event event :cause (.getCause event)}
          location-to   (-> event
                            (.getToTransform)
                            (.getLocation))
          location-from (-> event
                            (.getFromTransform)
                            (.getLocation))
          entity        (-> event
                            (.getTargetEntity))]
      (assoc result :entity entity
                    :location-to location-to
                    :location-from location-from)))
  InteractBlockEvent
  (destructure-event [^InteractBlockEvent event]
    (let [result        {:event event :cause (.getCause event)}
          block         (-> event
                            (.getTargetBlock))]
      (assoc result :block block)))
  DamageEntityEvent
  (destructure-event [^DamageEntityEvent event]
    (let [result        {:event event :cause (.getCause event)}
          entity        (-> event
                            (.getTargetEntity))
          damage-source (-> (:cause result)
                            (c/first-in DamageSource))
          damage-type   (-> damage-source
                            (.getType)
                            (as-damage-type))
          base-damage   (-> event
                            (.getBaseDamage))
          final-damage  (-> event
                            (.getFinalDamage))]
      (assoc result :entity entity
                    :damage-source damage-source
                    :damage-type damage-type
                    :base-damage base-damage
                    :final-damage final-damage)))
  TargetEntityEvent
  (destructure-event [^TargetEntityEvent event]
    (let [result        {:event event :cause (.getCause event)}
          entity        (-> event
                            (.getTargetEntity))]
      (assoc result :entity entity)))
  Event
  (destructure-event [^Event event]
    {:event event
     :cause (.getCause event)})
  )

(defn register-listener
  ([event-type fn] (register-listener event-type fn Order/DEFAULT))
  ([event-type fn order] (register-listener event-type fn order false))
  ([event-type fn order before-modifications]
   (let [proxy (proxy [EventListener] []
                 (handle [event]
                   (fn (destructure-event event))))]
     (-> (Sponge/getEventManager)
         (.registerListener (sp/get-plugin'), ^Class event-type, order, before-modifications, proxy)))))

(comment
  (-> (Sponge/getEventManager)
      (.unregisterPluginListeners (sp/get-plugin'))))