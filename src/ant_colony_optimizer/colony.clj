(ns ant-colony-optimizer.colony
  [:require [ant-colony-optimizer.tools :as t]
            [clojure.math.numeric-tower :as math]])

(defn create-colony
  [waypoints tours ants evap-rate]
  (let [keylist (t/create-keys (count waypoints))
        ones-map (zipmap keylist (repeat 1))
        zeros-map (zipmap keylist (repeat 0))
        evaporation-map (zipmap keylist (repeat (- 1 evap-rate)))]
    {:config {:alpha-coeff 1
              :beta-coeff 1.2
              :tour-coeff 1
              :tour-count tours
              :ant-count ants}
     :waypoints waypoints
     :pher-map (atom ones-map)
     :pher-update (atom zeros-map)
     :pher-reset ones-map
     :pher-update-reset zeros-map
     :evap evaporation-map
     :best-route (atom {:score 0 :path []})
     :stored-history (atom [])
     :local-optimal-score (atom 0)}))

(defn edge-weight
  ;; Compute the numerator component of the edge-selection probability from pt1 to pt2
  [pt1 pt2 coeffs waypoints pheremone-map]
  (let [nu (/ 1 (t/get-distance pt1 pt2 waypoints))
        tau ((t/get-edge pt1 pt2) pheremone-map)]
    (* (math/expt nu (:alpha-coeff coeffs))
       (math/expt tau (:beta-coeff coeffs)))))

(defn prob-weighting
  ;; Computes the probability threshold for the next node to be selected
  ;; Note: this returns the index of the selected value in rem-nodes ; NOT the selected nodeg
  ([active-node rem-nodes coeffs waypoints pheremone-map threshold-val]

   (let [weight-num (map #(edge-weight active-node % coeffs waypoints pheremone-map) rem-nodes)
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
  ([active-node rem-nodes coeffs waypoints pheremone-map ]
   (prob-weighting active-node rem-nodes coeffs waypoints pheremone-map (rand))))


(defn make-path
  ;; Generates the vector of waypoints visited for a single trip
  [waypoints coeffs pheremone-map]
  (let [open-nodes (shuffle (range 0 (count waypoints)))]
    (loop [nodes (conj [(first open-nodes)])
           remaining-nodes (t/remove-vec-element open-nodes 0)]
      (if (>= 1 (count remaining-nodes))
        (conj nodes (last remaining-nodes))

        (let [next-node-index (prob-weighting (last nodes) remaining-nodes coeffs waypoints pheremone-map)
              next-node (nth remaining-nodes next-node-index)]
          (recur (conj nodes next-node) (t/remove-vec-element remaining-nodes next-node-index)))))))

(defn path-score
  ;; Weighted score of route
  [path waypoints tour-coeff]
  (let [path2  (conj (subvec path 1) (first path))
        path-distances (reduce + (map #(t/get-distance %1 %2 waypoints) path path2))]
    (try
      (/ tour-coeff path-distances)
      (catch Exception e (str "caught exception: " (.getMessage e))))))

(defn ant
  ;; 1) Stores a closed loop route touching all waypoints
  ;; 2) Generates the route score (Q/Dist)
  ;; 3) Path-Offset shifts path indices by 1 (preparatory function for next step
  ;; 4) Generates a map of all the node-edges selected
  ;; 5) (evaluate-route) If this is the best global path, the global path/score is updated
  ;; 6) Adds pheremone deposits for this tour
  ;;
  ;; **Last 3 inputs to function are atoms
  [waypoints coeffs pheremone-map best-route local-optimal-score pheremone-update]
  (let [path (make-path waypoints coeffs pheremone-map)
        score (path-score path waypoints (:tour-coeff coeffs))
        path-offset (conj (subvec path 1) (first path))
        edges-to-update (map t/get-edge path path-offset)]

    (t/evaluate-route path score best-route)
    (if (> score @local-optimal-score)
      (reset! local-optimal-score score))
    (dorun (map #(swap! pheremone-update update-in [%] + score) edges-to-update)))
  )

(defn colony-tour
  ;; Ant tours not being called in parallel --TO FIX
  ;; Has N number of ants complete a full route and update the pheremone value
  [ant-count colony]
  (loop [counter (dec ant-count)]
    (ant   (:waypoints colony)
           (:config colony)
           @(:pher-map colony)
           (:best-route colony)
           (:local-optimal-score colony)
           (:pher-update colony))
    (if (zero? counter)
      (do
        (reset! (:pher-map colony) (merge-with + @(:pher-update colony) (merge-with * @(:pher-map colony) (:evap colony))))
        (reset! (:pher-update colony) (:pher-update-reset colony))
        ;;(println "Shortest path found: " (str (:path @(:best-route colony))))
        ;;(println "Path distance: " (str (t/get-route-distance (:path @(:best-route colony)) (:waypoints colony))))
        )
      (recur (dec counter)))))


(defn execute
  ;; Function to begin running the ant-tours
  [colony]
  (let [tour-total (-> colony :config :tour-count)]
    (loop [counter (dec tour-total)]
      (colony-tour (-> colony :config :ant-count) colony)
      (swap! (:stored-history colony) conj {:tour (- tour-total counter)
                                            :tour-best-score @(:local-optimal-score colony)
                                            :path (str (:path @(:best-route colony)))
                                            :weight-vec 1})
      (reset! (:local-optimal-score colony) 0)
      (if (zero? counter)
        (do
          (reset! (:pher-map colony) (:pher-reset colony))
          ;;(println "Shortest path found: " (str (:path @(:best-route colony))))
          ;;(println "Path distance: " (str (t/get-route-distance (:path @(:best-route colony)) (:waypoints colony))))
          )
        (recur (dec counter))))))