(ns sponge-clj.triggers
  (:require [sponge-clj.events :as ev]
            [sponge-clj.time :refer :all])
  (:import (org.spongepowered.api.event Event)
           (org.spongepowered.api.event.entity MoveEntityEvent)
           (java.util UUID)))

(def ^:private triggers (atom {}))
(def ^:private last-uses (atom {}))

(defn dispatch-event
  [^Event event]
  (let [filter-pred #(and (instance? (:event-type %) (:event event))
                          (if (and (some? (:predicate %)) (fn? (:predicate %))) (apply (:predicate %) [event]) true)
                          (>= (- (System/currentTimeMillis) (:delay %)) (get @last-uses (:id %) 0)))
        triggers    (filter filter-pred (vals @triggers))]
    (doseq [tr triggers]
      (swap! last-uses assoc (:id tr) (System/currentTimeMillis))
      (apply (:action tr) [event]))))

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
  (do (swap! last-uses assoc id (System/currentTimeMillis))
      (swap! triggers assoc id trigger)))

(defn def-walk-trigger
  [& {:as trigger}]
  (apply def-trigger (assoc trigger :event MoveEntityEvent)))

(ev/register-listener Event
                      (fn [event]
                        (dispatch-event event)))