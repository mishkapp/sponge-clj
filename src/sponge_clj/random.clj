(ns sponge-clj.random)

(defn from-range
  "Returns random number in range, inclusive"
  [from to]
  (int (+ (rand-int (+ 1 (- to from))) from)))

(defn chance
  "Return true if \"hits\" chance, chance is ratio"
  [val]
  (let [num (numerator val)
        den (denominator val)]
    (<= (range 1 den) num)))