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
  ;; BUG: Sometimes the same number will be passed as pt1 and pt2
  ;; Until fixed, this function's workaround is to return an obscenely large number
  (memoize
    (fn [pt1 pt2]
      (if (= pt1 pt2)
        99999999999999999999999999999999999999
        (math/sqrt (reduce + (map (comp #(math/expt % 2) -) pt1 pt2)))))))

(defn get-distance
  ;; Returns distance between 2 nodes in an array
  [n1 n2 waypoints]
  (let [nodeA (nth waypoints n1)
        nodeB (nth waypoints n2)]
    (euclidean-distance nodeA nodeB)))

(defn get-route-distance
  [path colony]
  (if (empty? path)
    nil
  (let [path2 (conj (subvec path 1) (first path))]
    (reduce + (map #(get-distance %1 %2 (:waypoints colony)) path path2)))))


(defn remove-vec-element
  ;; Removes selected index from a vector
  [node-array index]
  (if (= 0 index)
    (subvec node-array 1)
    (into (subvec node-array 0 index)
          (subvec node-array (inc index)))))

(defn evaluate-route
  ;; The best route is compared to every route taken & updated when a better one is found
  [path score best-route]
  (if (> score (:score @best-route))
    (let [better-route {:score score :path path}]
      (reset! best-route better-route))))

(defn create-keys
  ;; Given the waypoint count, return a vector of all the node-edges
  [waypoint-cnt]
  (let [nodesA (flatten (map #(repeat (- waypoint-cnt 1 %) %) (range waypoint-cnt)))
        nodesB (flatten(map  #(range (+ % 1) waypoint-cnt) (range waypoint-cnt)))]
    (mapv #(get-edge %1 %2) nodesA nodesB)))
