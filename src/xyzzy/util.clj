(ns xyzzy.util)

(defn lconj
  "Prepends `item` to vector `v`."
  [v item]
  (vec (concat [item] v)))

(defn update
  "Like `update-in`, but takes a single key `k` as its second argument instead
   of a key sequence."
  [m k f & args]
  (apply (partial update-in m [k] f) args))
