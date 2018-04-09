(defproject sponge-clj "_"
  :description "Clojure DSL for Sponge"
  :plugins [[me.arrdem/lein-git-version "2.0.8"]
            [lein-exec "0.3.7"]]
  :url "http://mishkapp.com"
  :license {:name "GNU General Public License v3.0"
            :url  "https://www.gnu.org/licenses/gpl-3.0.ru.html"}
  :repositories [["sponge" "https://repo.spongepowered.org/maven"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [io.replikativ/konserve "0.4.11"]
                 [org.clojure/tools.trace "0.7.9"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [org.clojure/clojure-contrib "1.2.0"]]
  :profiles {
             :dev      {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :provided {:dependencies [[org.spongepowered/spongeapi "7.0.0"]]}
             :uberjar  {:aot :all}
             }
  :git-version {
                :version-file "resources/assets/spongeclj/version.edn"
                :status-to-version
                              (fn [{:keys [tag version branch ahead ahead? dirty?] :as git}]
                                (assert (re-find #"\d+\.\d+\.\d+" tag)
                                  "Tag is assumed to be a raw SemVer version")
                                (if (and tag (not ahead?) (not dirty?))
                                  tag
                                  (let [[_ prefix patch] (re-find #"(\d+\.\d+)\.(\d+)" tag)
                                        patch (Long/parseLong patch)
                                        patch+ (inc patch)]
                                    (format "%s.%d-%s-SNAPSHOT" prefix patch+ branch))))
                }
  :aliases {"gen-mcmod" ["exec" "-p" "scripts/mcmod.clj"]
            "uberjar!"  ["do" ["gen-mcmod"] ["uberjar"]]})