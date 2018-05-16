(ns sponge-clj.commands
  (:require [sponge-clj.text :as t]
            [sponge-clj.sponge :as sponge]
            [clojure.contrib.reflect :as reflect])
  (:import (org.spongepowered.api.command.spec CommandExecutor CommandSpec)
           (org.spongepowered.api.command.args CommandElement GenericArguments CommandContext)
           (org.spongepowered.api Sponge)
           (org.spongepowered.api.plugin PluginContainer)
           (java.util List Map)
           (org.spongepowered.api.text Text)
           (org.spongepowered.api.command CommandResult CommandMapping)))

(defn- process-cmd-result
  [result-map]
  (if (nil? result-map) (CommandResult/empty)
                        (CommandResult/empty)))
(defn- any-alias?
  [^CommandMapping mapping aliases]
  (let [map-aliases (-> mapping
                        (.getAllAliases))
        filtered (filter #(contains? map-aliases %) aliases)]
    (not (empty? filtered))))

(defn- unregister-cmd
  [aliases]
  (let [mappings (-> (Sponge/getCommandManager)
                     (.getOwnedBy (sponge/get-plugin')))
        filtered (filter #(any-alias? % aliases) mappings)]
    (dorun (map #(-> (Sponge/getCommandManager)
                     (.removeMapping %))
                filtered))))

;not shure about reflection...
(defn- prepare-args
  [^CommandContext args]
  (let [raw-map (.asMap (reflect/get-field CommandContext "parsedArgs" args))
        keys    (map keyword (.keySet raw-map))
        vals    (map vec (.values raw-map))]
    (assoc (zipmap keys vals)
      :raw-context args)))

(defn cmd
  "Creates CommandSpec object"
  [& {:keys [executor permission arguments children description extended-description]
      :as   cmd-map}]
  {:pre [(or (some? executor)
             (some? children))]}
  (cond-> (CommandSpec/builder)
          (some? permission) (.permission permission)
          (some? executor) (.executor (proxy [CommandExecutor] []
                                        (execute [src args]
                                          (process-cmd-result
                                            (apply executor [src (prepare-args args)])))))
          (some? arguments) (.arguments ^"[Lorg.spongepowered.api.command.args.CommandElement;" (into-array CommandElement arguments))
          (some? description) (.description (t/text description))
          (some? extended-description) (.extendedDescription (t/text extended-description))
          (some? children) (.children children)
          true (.build)))

(defn def-cmd
  "Creates and register command. Commands that are already registered in sponge with same aliases will be unregistered"
  [& {:keys [aliases]
      :as   command}]
  (unregister-cmd aliases)
  (-> (Sponge/getCommandManager)
      (.register ^PluginContainer (sponge/get-plugin')
                 ^CommandSpec (apply cmd (apply concat (map vector (keys command) (vals command))))
                 ^List aliases)))

(defn string-arg
  "Require an argument to be a string."
  [key]
  (GenericArguments/string (t/text key)))

(defn remaining-joined-strings-arg
  "Concatenates all remaining arguments separated by spaces (useful for message commands)."
  [key]
  (GenericArguments/remainingJoinedStrings (t/text key)))

(defn bool-arg
  "Require an argument to be a boolean."
  [key]
  (GenericArguments/bool (t/text key)))

(defn integer-arg
  "Require an argument to be an integer."
  [key]
  (GenericArguments/integer (t/text key)))

(defn double-arg
  "Require an argument to be a double."
  [key]
  (GenericArguments/doubleNum (t/text key)))

(defn player-arg
  "Expect an argument to represent an online player. May return multiple players!"
  [key]
  (GenericArguments/player (t/text key)))

(defn player-or-source-arg
  "Like player-arg, but returns the sender of the command if no matching player was found."
  [key]
  (GenericArguments/playerOrSource (t/text key)))

(defn user-arg
  "Like player-arg, but returns a user instead of a player."
  [key]
  (GenericArguments/user (t/text key)))

(defn user-or-source-arg
  "Like player-or-source-arg, but returns a user instead of a player."
  [key]
  (GenericArguments/userOrSource (t/text key)))

(defn world-arg
  "Expect an argument to represent a world (also includes unloaded worlds)."
  [key]
  (GenericArguments/world (t/text key)))

(defn dimension-arg
  "Expect an argument to represent a dimension (END, NETHER, OVERWORLD)."
  [key]
  (GenericArguments/dimension (t/text key)))

(defn location-arg
  "Expect an argument to represent a Location."
  [key]
  (GenericArguments/location (t/text key)))

(defn vector3d-arg
  "Expect an argument to represent a Vector3d."
  [key]
  (GenericArguments/vector3d (t/text key)))

(defn cataloged-element-arg
  "Expect an argument that is a member of the specified CatalogType."
  [key catalog]
  (GenericArguments/catalogedElement (t/text key) catalog))

(defn choices-arg
  "Return an argument that allows selecting from a limited set of values."
  ([key args]
   (GenericArguments/choices (t/text key) args))
  ([^Text key ^Map args ^Boolean choices-in-usage]
   (GenericArguments/choices (t/text key) args choices-in-usage)))

(defn literal-arg
  "Expect a literal sequence of arguments (e.g. \"i\", \"luv\", \"u\": /cmd i luv u). Throws an error if the arguments do not match."
  ([key args]
   (GenericArguments/choices (t/text key) args))
  ([key args put-value]
   (GenericArguments/choices ^Text (t/text key) ^Map put-value ^Boolean args)))

(defn enum-arg
  "Require the argument to be a key under the provided enum."
  [key enum]
  (GenericArguments/enumValue (t/text key) enum))

(defn seq-arg
  "Builds a sequence of command elements (e.g. /cmd <arg1> <arg2> <arg3>)."
  [& values]
  (GenericArguments/seq values))

(defn repeated-arg
  "Require a given command element to be provided a certain number of times."
  [key times]
  (GenericArguments/repeated (t/text key) times))

(defn all-of-arg
  "Require all remaining args to match the provided command element."
  [elem]
  (GenericArguments/allOf elem))

(defn optional-arg
  "Make the provided command element optional. Throws an error if the argument is of invalid format and there are no more args."
  ([key]
    (GenericArguments/optional (t/text key)))
  ([key default]
    (GenericArguments/optional (t/text key) default)))

(defn optional-weak-arg
  "Make the provided command element optional. Does not throw an error if the argument is of invalid format and there are no more args."
  ([key]
   (GenericArguments/optionalWeak (t/text key)))
  ([key default]
   (GenericArguments/optionalWeak (t/text key) default)))

(defn first-passing-arg
  "Returns a command element that matches the first of the provided elements that parses (useful for command overloading, e.g. /settime <day|night|<number>)."
  [& values]
  (GenericArguments/firstParsing values))

(defn only-one-arg
  "Restricts the given command element to only insert one value into the context at the provided key."
  [elem]
  (GenericArguments/onlyOne elem))

(defn requiring-permission-arg
  "Requires the command sender to have the specified permission in order to use the given command argument"
  [elem permission]
  (GenericArguments/requiringPermission elem permission))

;todo: flags
