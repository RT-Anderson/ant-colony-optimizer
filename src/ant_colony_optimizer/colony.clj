(ns ant-colony-optimizer.colony
  [:require [ant-colony-optimizer.tools :as t]
            [clojure.math.numeric-tower :as math]])


(def waypoints
  (t/set-waypoints 4 20))

(defn create-colony
  [waypoint-list tours ants evap-rate]
  {:alpha-coeff 1
   :beta-coeff 1.2
   :tour-coeff 1
   :waypoints (t/set-waypoints (count waypoint-list) 20)
   :pher-map (atom (zipmap (t/create-keys (count waypoint-list)) (repeat 1)))
   :pher-update (atom (zipmap (t/create-keys (count waypoint-list)) (repeat 0)))
   :pher-reset (zipmap (t/create-keys (count waypoint-list)) (repeat 1))
   :pher-update-reset (zipmap (t/create-keys (count waypoint-list)) (repeat 1))
   :evap (zipmap (t/create-keys (count waypoint-list)) (repeat (- 1 evap-rate)))
   :tour-count tours
   :ant-count ants
   :best-route (atom {:score 0 :path []})})

(defn edge-weight
  ;; Compute the numerator component of the edge-selection probability from pt1 to pt2
  [pt1 pt2 colony]
  (let [nu (/ 1 (t/get-distance pt1 pt2 (:waypoints colony)))
        tau ((t/get-edge pt1 pt2) @(get-in colony [:pher-map ]))]
    (* (math/expt nu (:alpha-coeff colony))
       (math/expt tau (:beta-coeff colony)))))

(defn prob-weighting
  ;; Computes the probability threshold for the next node to be selected
  ;; Note: this returns the index of the selected value in rem-nodes ; NOT the selected nodeg
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
     selected-threshold-index))
  ([active-node rem-nodes colony]
   (prob-weighting active-node rem-nodes colony (rand))))



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
  (let [path2  (conj (subvec path 1) (first path))
        path-distances (reduce + (map #(t/get-distance %1 %2 (:waypoints colony)) path path2))]
    (try
      (/ (:tour-coeff colony) path-distances)
      (catch Exception e (str "caught exception: " (.getMessage e))))))

(defn ant
  ;; 1) Stores a closed loop route touching all waypoints
  ;; 2) Generates the route score (Q/Dist)
  ;; 3) Path-Offset shifts path indices by 1 (preparatory function for next step
  ;; 4) Generates a map of all the node-edges selected
  ;; 5) (evaluate-route) If this is the best global path, the global path/score is updated
  ;; 6) Adds pheremone deposits for this tour
  [colony]
  (let [path (make-path colony)
        score (path-score path colony)
        path-offset (conj (subvec path 1) (first path))
        edges-to-update (map t/get-edge path path-offset)]

    (t/evaluate-route path score (:best-route colony))
    (dorun (map #(swap! (:pher-update colony) update-in [%] + score) edges-to-update)))
  )

(defn colony-tour
  ;; Ant tours not being called in parallel --TO FIX
  ;; Has N number of ants complete a full route and update the pheremone value
  [ant-count colony]
  (loop [counter (dec ant-count)]
    (ant colony)
    (if (zero? counter)
      (do
        (reset! (:pher-map colony) (merge-with + (:evap colony) @(:pher-update colony) @(:pher-map colony)))
        (reset! (:pher-update colony) (:pher-update-reset colony))
        (println "Shortest path found: " (str (:path @(:best-route colony))))
        (println "Path distance: " (str (t/get-route-distance (:path @(:best-route colony)) colony))))
      (recur (dec counter)))))


(defn execute
  ;; Function to begin running the ant-tours
  [colony]
  (loop [counter (dec (:tour-count colony))]
    (colony-tour (:ant-count colony) colony)
    (if (zero? counter)
      (do
        (reset! (:pher-map colony) (:pher-reset colony))
        (println "Shortest path found: " (str (:path @(:best-route colony))))
        (println "Path distance: " (str (t/get-route-distance (:path @(:best-route colony)) colony))))
      (recur (dec counter)))))