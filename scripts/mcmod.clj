(ns mcmod
  (:require [clojure.edn :as edn]
            [clojure.data.json :as json]))

(let [version (:version (edn/read-string (slurp "./resources/assets/spongeclj/version.edn")))
      mcmod [{:modid "spongeclj"
              :name "Sponge-clj"
              :version version
              :description "Clojure in minecraft!"
              :url "http://mishkapp.com"
              :authorList ["mishkapp"]
              :dependencies ["spongeapi@7.0.0-SNAPSHOT"]
              :requiredMods ["spongeapi@7.0.0-SNAPSHOT"]}]]
  (spit "./resources/mcmod.info" (json/write-str mcmod)))

