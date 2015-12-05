(ns ant-colony-optimizer.tools
  [require [clojure.set :only rand-int]
           [clojure.math.numeric-tower :as math]])

(defn get-edge [n1 n2]
  ;; Converts the id of 2 nodes into a key;
  ;; used to retrieve and set values in the edge-map
  (if (neg? (compare n1 n2))
    (keyword (str n1 n2))
    (keyword (str n2 n1))))

(defn set-waypoints
  ;; Generates a random vector of [x y] points
  ;; Waypoint id is their array position
  [node-count max-value]
  (take node-count
        (repeatedly #(conj[]
                          (rand-int max-value)
                          (rand-int max-value)))))

(def euclidean-distance
  (memoize
    (fn [pt1 pt2]
      (math/sqrt (reduce + (map (comp #(math/expt % 2) -) pt1 pt2))))))

(defn get-distance
  ;; Returns distance between 2 nodes in an array
  [n1 n2 waypoints]
  (let [nodeA (nth waypoints n1)
        nodeB (nth waypoints n2)]
    (euclidean-distance nodeA nodeB)))

(defn remove-vec-element
  ;; Removes selected index from a vector
  [node-array index]
  (if (= 0 index)
    (subvec node-array 1)
    (into (subvec node-array 0 index)
          (subvec node-array (inc index)))))
