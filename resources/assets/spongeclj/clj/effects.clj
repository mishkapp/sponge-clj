(ns effects
  (:use [sponge-clj.particles]
        [sponge-clj.sponge]
        [sponge-clj.player]
        [sponge-clj.entity]
        [sponge-clj.world]
        [incanter.core])
  (:import (com.flowpowered.math.imaginary Quaterniond)
           (com.flowpowered.math.vector Vector3d)))

(defonce effects (atom {}))

(def pi 3.146)

(defn rotate-point
  [^Vector3d point rot]
  (let [xq (Quaterniond/fromAngleDegAxis ^Double (.getX rot) 1.0 0.0 0.0)
        yq (Quaterniond/fromAngleDegAxis ^Double (.getY rot) 0.0 1.0 0.0)
        zq (Quaterniond/fromAngleDegAxis ^Double (.getZ rot) 0.0 0.0 1.0)]
    (->> point
         (.rotate xq)
         (.rotate yq)
         (.rotate zq))))

(defn from-polar
  [r phi]
  [(* r (cos phi))
   (* r (sin phi))])

(defn to-radians
  [x]
  (/ (* pi x) 180.0))

; EFFECTS GOES HERE
(defn butterfly
  [theta]
  (- (exp (sin theta))
     (+ (* 2 (cos (* 4 theta)))
        (pow (sin (/ (- (* 2 theta) pi) 12)) 5))))

(defn pp
  [theta]
  (- (sin theta)) 1)

(defn butterfly-effect
  [player]
  (let [loc (get-loc player)
        time (-> (.getWorld player)
                 (.getProperties)
                 (.getWorldTime))
        body-rot (.getRotation player)
        min 0
        max (* pi 2)
        points 200
        delta (/ (- max min) points)]
    (doseq [i (range points)
            :let [phi (* delta i)
                  vec (from-polar (butterfly phi) phi)
                  vec3 (Vector3d. ^Double (first vec) ^Double (second vec) 0.0)
                  vec3 (.add vec3 (Vector3d. 0.0 1.0 ^Double (+ (/ (sin (+ (/ time 10) (* delta i))) 4) -0.5)))
                  rot (Vector3d. 0.0 ^Double (* -1 (.getY body-rot)) 0.0)
                  point (rotate-point vec3 rot)
                  point (.add point (loc->vec3d loc))
                  pe-loc (location (:world loc) (.getX point) (.getY point) (.getZ point))]]
      (spawn-particle (particle :redstone-dust {:color (color (* 64 (sin (* delta i))) 0 0)}) pe-loc)
      )))

(defn bteff
  [player]
  (butterfly-effect player))

(defn pp-effect
  [player]
  (let [loc (get-loc player)
        time (-> (.getWorld player)
                 (.getProperties)
                 (.getWorldTime))
        body-rot (.getRotation player)
        min 0
        max (* pi 2)
        points 100
        delta (/ (- max min) points)]
    (doseq [i (range points)
            :let [phi (* delta i)
                  vec (from-polar (pp phi) phi)
                  vec3 (Vector3d. ^Double (first vec) ^Double (second vec) 0.0)
                  vec3 (.add vec3 (Vector3d. 0.0 1.0 ^Double (+ (/ (sin (+ (/ time 10) (* delta i))) 4) -0.5)))
                  rot (Vector3d. 0.0 ^Double (* -1 (.getY body-rot)) 0.0)
                  point (rotate-point vec3 rot)
                  point (.add point (loc->vec3d loc))
                  pe-loc (location (:world loc) (.getX point) (.getY point) (.getZ point))]]
      (spawn-particle (particle :redstone-dust {:color (color 0 (* 64 (sin (* delta i))) 0)}) pe-loc)
      )))

(defn ppeff
  [player]
  (pp-effect player))
; AND END HERE

(defn tick-fn
  []
  (doseq [k (keys @effects)
          :let [player (get-player-by-uuid k)
                effect-fns (get @effects k)]]
    (doseq [efn effect-fns]
      (apply efn [player]))))

(defonce ticker (>><<sponge tick-fn 1 1))

(comment
  (reset! effects {})

  (swap! effects assoc (.getUniqueId (get-player-by-name "mishkapp")) [bteff])

  (swap! effects assoc (.getUniqueId (get-player-by-name "mishkapp")) [bteff ppeff])


  (.cancel ticker)

  (ns-unmap (find-ns 'test2) 'ticker)
  )