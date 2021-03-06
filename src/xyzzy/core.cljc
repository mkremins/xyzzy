(ns xyzzy.core
  (:refer-clojure
    :exclude [assoc descendants dissoc ensure find next remove replace update])
  (:require [xyzzy.util :refer [delete insert]]))

(def ^:private assoc*
  #?(:clj clojure.core/assoc :cljs cljs.core/assoc))

(def ^:private dissoc*
  #?(:clj clojure.core/dissoc :cljs cljs.core/dissoc))

(def ^:private update*
  #?(:clj clojure.core/update :cljs cljs.core/update))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; path movement
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- sibling* [path n]
  (when (>= n 0) (conj (pop path) n)))

(defn- down* [path]
  (conj path 0))

(defn- left* [path]
  (when (seq path) (sibling* path (dec (peek path)))))

(defn leftmost* [path]
  (when (seq path) (sibling* path 0)))

(defn- right* [path]
  (when (seq path) (sibling* path (inc (peek path)))))

(defn- up* [path]
  (when (seq path) (pop path)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; path and zipper helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- full-path [path]
  (interleave (repeat :children) path))

(defn node [{:keys [tree path]}]
  (get-in tree (full-path path)))

(defn branch?
  "Tests whether `node` is a branch node (i.e. permitted to have children)."
  [node]
  (contains? node :children))

(defn ensure [loc pred]
  (when (pred loc) loc))

(defn check
  "Tests whether `(:path loc)` points to an extant node in `(:tree loc)`,
  returning `loc` if the test passes and `nil` if it does not."
  [loc]
  (ensure loc (every-pred :path node)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; simple zipper movement
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn down [loc]
  (check (update* loc :path down*)))

(defn sibling [loc n]
  (check (update* loc :path sibling* n)))

(defn child [loc n]
  (-> loc down (sibling n)))

(defn left [loc]
  (check (update* loc :path left*)))

(defn leftmost [loc]
  (check (update* loc :path leftmost*)))

(defn right [loc]
  (check (update* loc :path right*)))

(defn up [loc]
  (check (update* loc :path up*)))

(defn rightmost [loc]
  (when-let [parent (node (up loc))]
    (sibling loc (-> parent :children count dec))))

(defn top [loc]
  (assoc* loc :path []))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; complex zipper movement
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn left-or-wrap
  "Returns the location immediately to the left of `loc` (if it exists), the
  rightmost sibling of `loc` (if it doesn't), or `loc` itself (if `loc` is at
  the top of the tree)."
  [loc]
  (or (left loc) (rightmost loc) loc))

(defn right-or-wrap
  "Returns the location immediately to the right of `loc` (if it exists), the
  leftmost sibling of `loc` (if it doesn't), or `loc` itself (if `loc` is at
  the top of the tree)."
  [loc]
  (or (right loc) (leftmost loc) loc))

(defn followers [loc direction]
  (->> loc (iterate direction) rest (take-while identity)))

(defn next [loc]
  (or (down loc) (right loc)
      (loop [loc' (up loc)]
        (or (right loc') (when-let [loc' (up loc')] (recur loc'))))))

(defn prev [loc]
  (if-let [left-loc (left loc)]
    (last (cons left-loc (followers left-loc (comp rightmost down))))
    (up loc)))

(defn children [loc]
  (when-let [down-loc (down loc)]
    (cons down-loc (followers down-loc right))))

(defn descendants [loc]
  (when-let [cs (children loc)]
    (concat cs (mapcat descendants cs))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; tree searching
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn find-next [loc pred direction]
  (first (filter pred (followers loc direction))))

(defn find [loc pred direction]
  (if (pred loc) loc (find-next loc pred direction)))

(defn find-next-node [loc pred direction]
  (find-next loc (comp pred node) direction))

(defn find-node [loc pred direction]
  (find loc (comp pred node) direction))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; in-place zipper modification
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn replace [loc new-node]
  (check (assoc-in loc
          (cons :tree (full-path (:path loc)))
          new-node)))

(defn edit [loc f & args]
  (replace loc (apply f (node loc) args)))

(defn edit-parent [loc f & args]
  (when-let [parent-loc (up loc)]
    (apply edit parent-loc f (peek (:path loc)) args)))

(defn assoc [loc & args]
  (apply edit loc assoc* args))

(defn dissoc [loc & args]
  (apply edit loc dissoc* args))

(defn update [loc & args]
  (apply edit loc update* args))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; subtree insertion & removal
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- insert-child* [parent n child]
  (update* parent :children insert n child))

(defn- remove-child* [parent n]
  (update* parent :children delete n))

(defn insert-child [loc n child]
  (edit loc insert-child* n child))

(defn insert-leftmost-child [loc child]
  (insert-child loc 0 child))

(defn insert-rightmost-child [loc child]
  (update loc :children conj child))

(defn insert-left [loc sib]
  (let [n (-> loc :path peek)]
    (-> loc up (insert-child n sib) (child (inc n)))))

(defn insert-right [loc sib]
  (let [n (-> loc :path peek inc)]
    (-> loc up (insert-child n sib) (child (dec n)))))

(defn remove [loc]
  (let [n    (-> loc :path peek)
        loc' (-> loc up (edit remove-child* n))]
    (if (-> loc' node :children empty?)
      loc'
      (child loc' (max (dec n) 0)))))

(defn remove-child [loc n]
  (edit loc remove-child* n))
