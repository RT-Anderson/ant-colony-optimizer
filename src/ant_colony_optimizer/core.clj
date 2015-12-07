(ns ant-colony-optimizer.core
  [:require [ant-colony-optimizer.tools :as t]
            [ant-colony-optimizer.colony :as aco :only [create-colony execute]]])

(def waypoints
  (distinct (t/set-waypoints 40 20)))
waypoints

(def colony
  ;; Variables
  ;; List of waypoints
  ;; Number of tours
  ;; Number of ants
  ;; Pheremone evaporation rate (>1)
  (aco/create-colony waypoints 10 20 0.20))
(aco/execute colony)
