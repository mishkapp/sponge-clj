(ns sponge-clj.random)

(defn from-range
  "Returns random number in range, inclusive"
  [from to]
  (int (+ (rand-int (+ 1 (- to from))) from)))

(defn chance
  "Return true if \"hits\" chance, chance is ratio or float [0..1]"
  [val]
  (if (ratio? val)
    (let [num (numerator val)
          den (denominator val)]
      (<= (from-range 1 den) num)))
    (<= (rand) val)
  )