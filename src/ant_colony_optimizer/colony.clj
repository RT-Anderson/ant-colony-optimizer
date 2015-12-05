(ns ant-colony-optimizer.colony
  [:require [ant-colony-optimizer.tools :as t]
            [clojure.math.numeric-tower :as math]])

(def colony
  {:alpha-coeff 1
   :beta-coeff  1.5
   :tour-coeff  1
   :waypoints   '([16 6] [11 5] [12 5] [2 12])
   :pher-map    {:01 1 :02 1 :03 1
                 :12 1 :13 1
                 :23 1}
   :pher-update {:01 0 :02 0 :03 0
                 :12 0 :13 0
                 :23 0}
   :tour-count  5
   :ant-count   3})

(def waypoints
  (t/set-waypoints 4 20))


;;Have a single ant complete a route


;;provide list of available nodes
(def open-nodes (shuffle (range 0 (count waypoints))))

;; Compute weight

;; Select first node

(def wpt1 (first open-nodes))
(def open-nodes (t/remove-vec-element open-nodes 1))
(def wpt2 (first open-nodes))
wpt1
wpt2
open-nodes


(defn edge-weight
  ;; Compute the numerator component of the edge-selection probability from pt1 to pt2
  [pt1 pt2 colony]
  (let [nu (/ 1 (t/get-distance pt1 pt2 (:waypoints colony)))
        tau (get-in colony [:pher-map (t/get-edge pt1 pt2)])]
    (* (math/expt nu (:alpha-coeff colony))
       (math/expt tau (:beta-coeff colony)))))

(defn prob-weighting
  ;; Computes the probability threshold for the next node to be selected
  ;; Note: this returns the index of the selected value in rem-nodes ; NOT the selected node
  ([active-node rem-nodes colony threshold-val]
   (let [weight-num (map #(edge-weight active-node % colony) rem-nodes)
         weight-denom (reduce + weight-num)
         weight (mapv #(/ % weight-denom) weight-num)
         weight-cnt (count weight)
         threshold (loop [incremented-weight weight
                          counter 1]
                     (if (= weight-cnt counter)
                       incremented-weight
                       (recur (#(assoc %2 %1 (+ (nth %2 (dec %1)) (nth %2 %1))) counter incremented-weight)
                              (inc counter))))
         selected-threshold-index (.indexOf threshold (first (filter #(< threshold-val %) threshold)))]
     ;; (.println (System/out) (str threshold-val))
     selected-threshold-index))
  ([active-node rem-nodes colony]
   (prob-weighting active-node rem-nodes colony (rand))))

;;Select the next node
;;Update available list


(defn make-path
  ;; Generates the vector of waypoints visited for a single trip
  [colony]
  (let [open-nodes (shuffle (range 0 (count (:waypoints colony))))]
    (loop [nodes (conj [(first open-nodes)])
           remaining-nodes (t/remove-vec-element open-nodes 0)]
      (if (>= 1 (count remaining-nodes))
        (conj nodes (last remaining-nodes))

        (let [next-node-index (prob-weighting (last nodes) remaining-nodes colony)
              next-node (nth remaining-nodes next-node-index)]
          (recur (conj nodes next-node) (t/remove-vec-element remaining-nodes next-node-index)))))))

(defn path-score
  ;; Weighted score of route
  [path colony]
  (let [path2  (conj (subvec path 1) (first path))]
  (/ (:tour-coeff colony) (reduce + (map #(t/get-distance %1 %2 (:waypoints colony)) path path2)))))

(defn ant
  [colony]
  (let [path (make-path colony)
        score (path-score path colony)]
    score))

(map ant [colony colony colony colony colony])
(make-path colony)

