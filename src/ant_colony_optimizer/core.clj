(ns ant-colony-optimizer.core
  [:require [ant-colony-optimizer.tools :as t :only [set-waypoints]]
            [ant-colony-optimizer.colony :as aco :only [create-colony execute]]
            [ant-colony-optimizer.aco-visualizer :as vis :only [build-sketch]]])

(defn create-waypoints
  [waypoint-count max-screen-dimension]
  (distinct (t/set-waypoints waypoint-count max-screen-dimension)))

(defn create-colony
  [waypoint-list tour-count ant-count evap-rate]
  (aco/create-colony waypoint-list tour-count ant-count evap-rate))

(defn run-aco [colony] (aco/execute colony))

(defn draw-waypoints [waypoint-list max-screen-dimension]
  (vis/sketch-waypoints waypoint-list max-screen-dimension))

(defn draw-path [waypoint-list colony max-screen-dimension]
  (vis/sketch-route waypoint-list colony max-screen-dimension))

;;(def w (create-waypoints 30 400))
;;(def c (create-colony w 40 25 0.6))
;;(run-aco c)
;;(def v (draw-path w c 400))