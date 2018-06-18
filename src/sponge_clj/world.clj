(ns sponge-clj.world
  (:use [sponge-clj.sponge])
  (:import (org.spongepowered.api.world Location World)
           (org.spongepowered.api Sponge)
           (org.spongepowered.api.block BlockState BlockType BlockTypes)
           (com.flowpowered.math.vector Vector3d)))

(defn block-location-equals?
  [loc1 loc2]
  (and (= (:world loc1) (:world loc2))
       (= (int (:x loc1)) (int (:x loc2)))
       (= (int (:y loc1)) (int (:y loc2)))
       (= (int (:z loc1)) (int (:z loc2)))))

(defn location-equals?
  [loc1 loc2 accuracy]
  (and (= (:world loc1) (:world loc2))
       (<= (Math/abs ^double (- (:x loc1) (:x loc2))) accuracy)
       (<= (Math/abs ^double (- (:y loc1) (:y loc2))) accuracy)
       (<= (Math/abs ^double (- (:z loc1) (:z loc2))) accuracy)))

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
   (do (-> (Sponge/getServer)
         (.getWorld ^String (:world loc))
         (.orElse nil)))))

(defn get-world-name
  [loc]
  (-> (get-world loc)
      (.getName)))

(defn as-sponge-location
  (^Location [loc]
    (-> (get-world loc)
        (.getLocation ^double (:x loc)
                      ^double (:y loc)
                      ^double (:z loc)))))

(defn get-biome
  [loc]
  (-> (get-world loc)
      (.getBiome (:x loc) (:y loc) (:z loc))))

(defn block-by-id
  (^BlockState [id]
   (.getDefaultState (get-catalog-type BlockType id))))

(defn get-block
  (^BlockState [loc]
   (-> (get-world loc)
       (.getBlock (:x loc) (:y loc) (:z loc)))))

(defn set-block
  [loc block]
  (let [block (if (string? block) (block-by-id block) block)]
    (-> (as-sponge-location loc)
        (.setBlock block))))

(defn loc->vec3d
  [loc]
  (Vector3d. ^Double (:x loc) ^Double (:y loc) ^Double (:z loc)))