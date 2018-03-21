(ns sponge-clj.text
  (:import (org.spongepowered.api.text.serializer TextSerializers)))

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