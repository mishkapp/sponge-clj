(ns sponge-clj.core
  (:require [sponge-clj.logger :as logger]
            [clojure.tools.nrepl.server :refer (start-server stop-server)]
            [sponge-clj.util :as util]
            [sponge-clj.text :as text]
            [sponge-clj.commands :as cmd]
            [sponge-clj.triggers :as triggers]
            [clojure.java.io :as io]
            [sponge-clj.database :as db]
            [clojure.edn :as edn]
            [sponge-clj.script :as s])
  (:import (org.spongepowered.api.event Listener)
           (org.spongepowered.api.plugin Plugin PluginContainer)
           (com.google.inject Inject)
           (java.nio.file Path)
           (org.spongepowered.api Sponge)
           (org.spongepowered.api.text Text)
           (org.spongepowered.api.command CommandSource)
           (org.spongepowered.api.data DataRegistration)
           (sponge_clj LambdaMobData LambdaMobData$Immutable LambdaMobData$Builder)))

(def ^:private ^PluginContainer plugin (atom nil))

(defn get-plugin
  []
  (if (nil? @plugin)
    (reset! plugin (-> (Sponge/getPluginManager)
                       (.getPlugin "spongeclj")
                       (.get)))
    @plugin))

(def ^:private ^Path private-config-dir (atom nil))

(def ^:private config (atom {}))

(def repl-handle (atom nil))

(defn start-repl [host port]
  (logger/info "Starting repl on host: %s, port %s" host port)
  (cond
    @repl-handle
    {:msg "you tried to start a(nother) repl while one was already started"}
    (util/port-in-use? port host)
    {:msg (format "REPL already started or port %s:%s is in use" host port)}
    :else
    (do
      (reset! repl-handle (start-server :host host :port port))
      {:msg (format "Started repl on host: %s, port %s" host port)})))

(defn stop-repl
  []
  (cond
    (nil? @repl-handle) {:msg "you tried to stop REPL when it was not running"}
    :else
    (try
      (do
        (stop-server @repl-handle)
        {:msg "REPL stopped"})
      (finally
        (reset! repl-handle nil)))))

(gen-class :name ^{Plugin {
                           :id          "spongeclj"
                           :name        "Sponge-clj"
                           :description "Clojure in Minecraft!"
                           :url         "http://mishkapp.com"
                           :authors     ["mishkapp"]
                           }}
           sponge_clj.Core
           :prefix "main-"
           :methods [[^{Listener {}}
                     onServerStart [org.spongepowered.api.event.game.state.GameStartedServerEvent] void]
                     [^{Listener {}}
                     onRegister [org.spongepowered.api.event.game.state.GamePreInitializationEvent] void]
                     ;Injects
                     [^{Inject {}}
                     setLogger [org.slf4j.Logger] void]]
           :constructors {[] []})

(defn- load-ns
  []
  (let [files (file-seq (-> @private-config-dir
                            (.resolve "clj")
                            (.toFile)))
        files (filter #(re-matches #"(.)*\.clj" (str %)) files)]
    (doseq [f files]
      (do
        (logger/info (str "Loading ns file: " f))
        (load-file (str f))))))

(defn eval-cmd
  [^CommandSource src args]
  (let [expr (first (:expression args))]
    (.sendMessage src ^Text (text/text (str (eval (read-string expr)))))))

(def reload-ns-cmd
  (cmd/cmd
    :description "Reload all files from clj dir"
    :executor (fn [src args] (load-ns))
    ))

(def reload-scripts-cmd
  (cmd/cmd
    :description "Reload all files from scripts dir"
    :executor (fn [src args] (s/load-scripts))))


(defn init-commands
  []
  (cmd/def-cmd
    :aliases ["eval"]
    :executor eval-cmd
    :arguments [(cmd/remaining-joined-strings-arg "expression")]
    :permission "spongeclj.eval"
    :description "Raw clojure evaluation")

  (cmd/def-cmd
    :aliases ["reload"]
    :children {
               ["ns", "namespace"] reload-ns-cmd
               ["scripts"]         reload-scripts-cmd
               }
    :permission "spongeclj.reload"
    :description "Reload scripts"))

(defn- init-config
  []
  (-> (get-plugin)
      (.getAsset "config.edn")
      (.get)
      (.copyToDirectory @private-config-dir))
  (reset! config (edn/read-string (slurp (str (.toString @private-config-dir) "/config.edn")))))

(defn init-db
  []
  (let [db-dir-file (io/as-file db/db-dir)]
    (cond (not (.exists db-dir-file))
          (.mkdir db-dir-file))))

(defn main-onServerStart
  [this event]
  (reset! private-config-dir (-> (Sponge/getConfigManager)
                                 (.getPluginConfig (get-plugin))
                                 (.getDirectory)))
  (init-config)
  (cond (get-in @config [:repl :enabled?])
        (start-repl (get-in @config [:repl :host]) (get-in @config [:repl :port])))
  (init-db)
  (triggers/init)
  (-> (get-plugin)
      (.getAsset "clj/test.clj")
      (.get)
      (.copyToDirectory (-> @private-config-dir
                            (.resolve "clj"))))
  (load-ns)
  (-> (get-plugin)
      (.getAsset "scripts/test.clj")
      (.get)
      (.copyToDirectory (-> @private-config-dir
                            (.resolve "scripts"))))
  (s/load-scripts)
  (init-commands)
  (logger/info "Sponge-clj enabled!"))

(defn main-setLogger
  [this lg]
  (reset! logger/logger' lg))

(defn main-onRegister
  [this event]
  (LambdaMobData/initKey)
  (sponge-clj.keys/init-keys)
  (-> (DataRegistration/builder)
      (.dataName "Lambda mob id")
      (.manipulatorId "lambda_mob_id")
      (.dataClass LambdaMobData)
      (.immutableClass LambdaMobData$Immutable)
      (.builder (LambdaMobData$Builder.))
      (.buildAndRegister (sponge-clj.sponge/get-plugin'))))
