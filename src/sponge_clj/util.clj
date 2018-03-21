(ns sponge-clj.util
  (:use [sponge-clj.text])
  (:import (org.spongepowered.api.entity.living.player Player)
           (java.io IOException)
           (java.net ServerSocket InetSocketAddress)))

(defn send-message
  "Sends message to player"
  [player message]
  (if (instance? Player player)
    (.sendMessage player (to-text message))))

(defmacro when-let*
  "Multiple binding version of when-let"
  [bindings & body]
  (if (seq bindings)
    `(when-let [~(first bindings) ~(second bindings)]
       (when-let* ~(vec (drop 2 bindings)) ~@body))
    `(do ~@body)))

(defn port-in-use? [port bind]
  (let [bind-addr (if (InetSocketAddress. bind port) (InetSocketAddress. port))]
    (try
      (with-open [ss (ServerSocket. port 0 (.getAddress bind-addr))] false)
      (catch IOException e true))))