(ns sponge-clj.sponge
  (:import (org.spongepowered.api Sponge)
           (clojure.lang IFn)
           (org.spongepowered.api.plugin PluginContainer)
           (org.spongepowered.api.entity.living.player Player)))

(def ^:private ^PluginContainer plugin (atom nil))

(defn get-plugin'
  []
  (if (nil? @plugin)
    (reset! plugin (-> (Sponge/getPluginManager)
                       (.getPlugin "spongeclj")
                       (.get)))
    @plugin))

(defn get-private-config-dir
  []
  (-> (Sponge/getConfigManager)
      (.getPluginConfig (get-plugin'))
      (.getDirectory)))

(defn player?
  [source]
  (instance? Player source))

(defn get-catalog-type
  "Returns catalog type from sponge registry or nil if it is absent"
  [type id]
  (-> (Sponge/getGame)
      (.getRegistry)
      (.getType type (str id))
      (.orElse nil)))

(defn >sponge
  "Asynchrounous executing now or after `delay` ticks"
  ([^IFn fn]
   (-> (Sponge/getScheduler)
       (.createTaskBuilder)
       (.async)
       (.execute fn)
       (.submit (get-plugin'))))
  ([^IFn fn delay]
   (-> (Sponge/getScheduler)
       (.createTaskBuilder)
       (.async)
       (.delayTicks delay)
       (.execute fn)
       (.submit (get-plugin')))))

(defn >>sponge
  "Synchronous executing now (1 tick) or after `delay` ticks"
  ([^IFn fn]
   (-> (Sponge/getScheduler)
       (.createTaskBuilder)
       (.delayTicks 1)
       (.execute fn)
       (.submit (get-plugin'))))
  ([^IFn fn delay]
   (-> (Sponge/getScheduler)
       (.createTaskBuilder)
       (.delayTicks delay)
       (.execute fn)
       (.submit (get-plugin')))))

(defn >><<sponge
  [^IFn fn delay interval]
  (-> (Sponge/getScheduler)
      (.createTaskBuilder)
      (.delayTicks delay)
      (.intervalTicks interval)
      (.execute fn)
      (.submit (get-plugin'))))

(defn process-command
  [cmd]
  (>>sponge
    #(-> (Sponge/getCommandManager)
        (.process (.getConsole (Sponge/getServer)) cmd))))

