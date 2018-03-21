(ns sponge-clj.world
  (:import (org.spongepowered.api.world Location World)
           (org.spongepowered.api Sponge)))

(defn block-location-equals?
  [loc1 loc2]
  (and (= (:world loc1) (:world loc2))
       (= (int (:x loc1)) (int (:x loc2)))
       (= (int (:y loc1)) (int (:y loc2)))
       (= (int (:z loc1)) (int (:z loc2)))))

(defn location-equals?
  [loc1 loc2 accuracy]
  (and (= (:world loc1) (:world loc2))
       (<= (Math/abs (- (:x loc1) (:x loc2))) accuracy)
       (<= (Math/abs (- (:y loc1) (:y loc2))) accuracy)
       (<= (Math/abs (- (:z loc1) (:z loc2))) accuracy)))

(defn location
  ([^Location loc]
   {:pre (isa? World (.getExtent loc))}
    {:world (.getName (.getExtent loc))
     :x (.getX loc)
     :y (.getY loc)
     :z (.getZ loc)})
  ([world x y z]
   {:world world :x x :y y :z z}))

(defn get-world
  (^World [loc]
    (-> (Sponge/getServer)
        (.getWorld (:world loc))
        (.get))))

(defn as-sponge-location
  (^Location [loc]
    (-> (get-world loc)
        (.getLocation (:x loc) (:y loc) (:z loc)))))

