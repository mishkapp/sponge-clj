(ns sponge-clj.cause
  (:import (org.spongepowered.api.event.cause Cause)))


(defn first-in
  [^Cause cause ^Class type]
  (-> cause
      (.first type)
      (.orElse nil)))

(defn last-in
  [^Cause cause ^Class type]
  (-> cause
      (.last type)
      (.orElse nil)))

(defn before-in
  [^Cause cause ^Class type]
  (-> cause
      (.before type)
      (.orElse nil)))

(defn after-in
  [^Cause cause ^Class type]
  (-> cause
      (.after type)
      (.orElse nil)))

(defn root
  [^Cause cause]
  (-> cause
      (.root)))


