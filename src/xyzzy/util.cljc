(ns xyzzy.util)

(defn delete
  "Deletes the item at `idx` in the vector `v`."
  [v idx]
  (if (and (= (count v) 1) (= idx 0))
    (empty v)
    (into (subvec v 0 idx) (subvec v (inc idx)))))

(defn insert
  "Inserts `item` into vector `v` at `idx`, pushing whatever was already at
  `idx` one slot to the right."
  [v idx item]
  (into (conj (subvec v 0 idx) item) (subvec v idx)))
