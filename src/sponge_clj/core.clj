(ns sponge-clj.core
  (:require [sponge-clj.logger :as logger]
            [clojure.tools.nrepl.server :refer (start-server stop-server)]
            [sponge-clj.util :as util]
            [sponge-clj.text :as text]
            [sponge-clj.commands :as cmd]
            [sponge-clj.triggers :as triggers]
            [clojure.java.io :as io]
            [sponge-clj.database :as db])
  (:import (org.spongepowered.api.event Listener)
           (org.spongepowered.api.plugin Plugin PluginContainer)
           (com.google.inject Inject)
           (java.nio.file Path)
           (org.spongepowered.api Sponge)
           (org.spongepowered.api.text Text)
           (org.spongepowered.api.command CommandSource)))

(def ^:private ^PluginContainer plugin (atom nil))

(defn get-plugin
  []
  (if (nil? @plugin)
    (reset! plugin (-> (Sponge/getPluginManager)
                       (.getPlugin "spongeclj")
                       (.get)))
    @plugin))

(def ^:private ^Path private-config-dir (atom nil))

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
                     ;Injects
                     [^{Inject {}}
                     setLogger [org.slf4j.Logger] void]]
           :constructors {[] []})

(defn eval-cmd
  [^CommandSource src args]
  (let [expr (first (:expression args))]
    (.sendMessage src ^Text (text/to-text (str (eval (read-string expr)))))))

(defn reload-cmd
  [^CommandSource src args]
  (load-file (-> @private-config-dir
                 (.resolve "test.clj")
                 (.toString))))

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
    :executor reload-cmd
    :permission "spongeclj.reload"
    :description "Reload scripts"))

(defn init-db
  []
  (let [db-dir-file (io/as-file db/db-dir)]
    (cond (not (.exists db-dir-file))
          (.mkdir db-dir-file))))

(defn main-onServerStart
  [this event]
  (start-repl "0.0.0.0" 40000)
  (init-db)
  (triggers/init)
  (reset! private-config-dir (-> (Sponge/getConfigManager)
                                 (.getPluginConfig (get-plugin))
                                 (.getDirectory)))
  (-> (get-plugin)
      (.getAsset "clj/test.clj")
      (.get)
      (.copyToDirectory @private-config-dir))
  (load-file (-> @private-config-dir
                 (.resolve "test.clj")
                 (.toString)))
  (init-commands)
  (logger/info "Sponge-clj enabled!"))

(defn main-setLogger
  [this lg]
  (reset! logger/logger' lg))
