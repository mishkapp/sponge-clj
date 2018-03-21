(defproject sponge-clj "0.1.0-SNAPSHOT"
  :description "Clojure DSL for Sponge"
  :url "http://mishkapp.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories [["sponge" "https://repo.spongepowered.org/maven"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [io.replikativ/konserve "0.4.11"]
                 [org.clojure/tools.trace "0.7.9"]
                 [org.clojure/tools.nrepl "0.2.3"]]
  :profiles {
             :dev {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :provided {:dependencies [[org.spongepowered/spongeapi "7.0.0"]]}
             :uberjar {:aot :all}
             })
