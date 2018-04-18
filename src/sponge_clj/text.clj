(ns sponge-clj.text
  (:require [clojure.java.io :as io])
  (:import (org.spongepowered.api.text.serializer TextSerializers)
           (org.spongepowered.api.text.format TextColors TextStyles)
           (org.spongepowered.api.text Text)
           (org.spongepowered.api.text.action TextActions)
           (org.spongepowered.api.entity Entity)
           (java.util.function Consumer)))

(defn to-text
  "Converts string to Sponge text"
  [str]
  (-> (TextSerializers/FORMATTING_CODE)
      (.deserialize str)))

(defn to-string
  "Converts Sponge text to string"
  [text]
  (-> (TextSerializers/FORMATTING_CODE)
      (.serialize text)))

(def colors
  {:aqua         TextColors/AQUA
   :black        TextColors/BLACK
   :blue         TextColors/BLUE
   :dark-aqua    TextColors/DARK_AQUA
   :dark-blue    TextColors/DARK_BLUE
   :dark-gray    TextColors/DARK_GRAY
   :dark-green   TextColors/DARK_GREEN
   :dark-purple  TextColors/DARK_PURPLE
   :dark-red     TextColors/DARK_RED
   :gold         TextColors/GOLD
   :gray         TextColors/GRAY
   :green        TextColors/GREEN
   :light-purple TextColors/LIGHT_PURPLE
   :red          TextColors/RED
   :white        TextColors/WHITE
   :yellow       TextColors/YELLOW
   :reset        TextColors/RESET
   }

  (defn text-color
    "Creates a color for formatting"
    [color]
    (get colors color TextColors/WHITE)))

(def styles
  {:none          TextStyles/NONE
   :obfuscated    TextStyles/OBFUSCATED
   :bold          TextStyles/BOLD
   :strikethrough TextStyles/STRIKETHROUGH
   :underline     TextStyles/UNDERLINE
   :italic        TextStyles/ITALIC
   :reset         TextStyles/RESET
   })

(defn text-style
  "Creates a style for formatting"
  [style]
  (get styles style TextStyles/NONE))

(defn text-click-url
  "Text that will ask the player to open an URL when it is clicked"
  [url]
  (TextActions/openUrl (io/as-url url)))

(defn text-click-cmd
  "Text that will run a command on the client when it is clicked"
  [cmd]
  (TextActions/runCommand cmd))

(defn text-click-suggest
  "Text that will suggest the player a command when it is clicked"
  [cmd]
  (TextActions/suggestCommand cmd))

(defn text-click-execute
  "Text that will execute the given function on the server when clicked.
  The callback will expire after some amount of time (not particularly instantly, but not like overnight really either)."
  [fn]
  (TextActions/executeCallback (reify Consumer
                                 (accept [this cs]
                                   (apply fn cs)))))

(defn text-hover-text
  "Text that will show a text on the client when it is hovered."
  [text]
  (TextActions/showText text))

(defn text-hover-item
  "Text that will show information about an item when it is hovered."
  [item]
  (TextActions/showItem item))

(defn text-hover-entity
  "Text that will show information about an entity when it is hovered"
  [entity name]
  (TextActions/showEntity ^Entity entity ^String name))

(defn text-shift-click-insert
  "Text that will insert text at the current cursor position in the chat when it is shift-clicked."
  [str]
  (TextActions/insertText str))

(defn text
  "Creates text"
  [& xs]
  (Text/of (into-array Object xs)))