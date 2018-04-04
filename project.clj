(defproject sponge-clj "0.2.1"
  :description "Clojure DSL for Sponge"
  :url "http://mishkapp.com"
  :license {:name "GNU General Public License v3.0"
            :url "https://www.gnu.org/licenses/gpl-3.0.ru.html"}
  :repositories [["sponge" "https://repo.spongepowered.org/maven"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [io.replikativ/konserve "0.4.11"]
                 [org.clojure/tools.trace "0.7.9"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [org.clojure/clojure-contrib	"1.2.0"]]
  :profiles {
             :dev      {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :provided {:dependencies [[org.spongepowered/spongeapi "7.0.0"]]}
             :uberjar  {:aot [sponge-clj.core]}
             })
