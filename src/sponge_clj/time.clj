(ns sponge-clj.time)

(defn ticks
  "Ticks to milliseconds"
  [t]
  (* t 50))

(defn seconds
  "Seconds to milliseconds"
  [s]
  (* s 1000))

(defn minutes
  "Minutes to milliseconds"
  [m]
  (seconds (* m 60)))

(defn hours
  "Hours to milliseconds"
  [h]
  (minutes (* h 60)))

(defn days
  "Days to milliseconds"
  [d]
  (hours (* d 24)))

(defn current-time
  "Current time in milliseconds"
  []
  (System/currentTimeMillis))
