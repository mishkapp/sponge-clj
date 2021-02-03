(ns sponge-clj.script
  (:require [clojure.string :as s]
            [sponge-clj.sponge :as sp]))

(def sandbox (binding [*ns* (create-ns 'sponge-clj.script.sandbox)]
               (do
                 (ns sponge-clj.script.sandbox
                   (:use
                     [sponge-clj.cause]
                     [sponge-clj.commands]
                     [sponge-clj.database]
                     [sponge-clj.enchantments]
                     [sponge-clj.entity]
                     [sponge-clj.events]
                     [sponge-clj.items]
                     [sponge-clj.keys]
                     [sponge-clj.lambda-items]
                     [sponge-clj.lambda-mobs]
                     [sponge-clj.logger]
                     [sponge-clj.menu]
                     [sponge-clj.particles]
                     [sponge-clj.player]
                     [sponge-clj.potion-effects]
                     [sponge-clj.random]
                     [sponge-clj.sponge]
                     [sponge-clj.text]
                     [sponge-clj.time]
                     [sponge-clj.triggers]
                     [sponge-clj.util]
                     [sponge-clj.world]
                     ))
                 *ns*)))

(defn with-ns
  [ns expr]
  (binding [*ns* ns]
    (eval expr)))

(with-ns sandbox
         (require
           '(clojure [string :as str])))

(defn load-script-from-file
  [f]
  (let [content (read-string (str \[ (slurp f) \]))]
    (sponge-clj.logger/info (str "Loading script file: " (.getPath f)))
    (doseq [expr content]
      (try
        (with-ns sandbox expr)
        (catch Exception e
          (sponge-clj.logger/warn (str "Error occured while loading script " (.getPath f)))
          (sponge-clj.logger/warn (str "Problem expression: " expr))
          (.printStackTrace e)
          (throw (RuntimeException.))
          ))
      )
    ))

(defn load-scripts []
  (let [scripts-dir (-> (sp/get-private-config-dir)
                        (.resolve "scripts"))
        scripts (sort-by #(.getPath %)
                         (filter #(and (.isFile %) (s/ends-with? (.getPath %) ".clj"))
                                 (file-seq (.toFile scripts-dir)))
                         )]
    (try
      (doseq [s scripts]
        (load-script-from-file s))
      (catch RuntimeException e
        (sponge-clj.logger/warn "Aborting script loading")))
    ))