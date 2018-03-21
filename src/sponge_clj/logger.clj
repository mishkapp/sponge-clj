(ns sponge-clj.logger
  (:import (org.slf4j Logger)))

(def ^Logger logger' (atom nil))

(defn info
  [msg & args]
  (.info @logger' (str msg args)))

(defn warn
  [msg & args]
  (.warn @logger' (str msg args)))

(defn debug
  [msg & args]
  (.debug @logger' (str msg args)))

(defn error
  [msg & args]
  (.error @logger' (str msg args)))

(defn trace
  [msg & args]
  (.trace @logger' (str msg args)))
