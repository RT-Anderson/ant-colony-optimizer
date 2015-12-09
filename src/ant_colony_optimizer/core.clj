(ns ant-colony-optimizer.core
  [:require [ant-colony-optimizer.tools :as t]
            [ant-colony-optimizer.colony :as aco :only [create-colony execute]]])

(def waypoints
  (distinct (t/set-waypoints 30 200)))
waypoints

(def colony
  ;; Variables
  ;; List of waypoints
  ;; Number of tours
  ;; Number of ants
  ;; Pheremone evaporation rate (>1)
  (aco/create-colony waypoints 20 15 0.40))

(aco/execute colony)

@(:stored-history colony)
(t/get-route-distance (:path @(:best-route colony)) waypoints)
