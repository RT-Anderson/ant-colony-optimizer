(ns ant-colony-optimizer.aco-visualizer
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(defn setup []
  (q/frame-rate 30)
  (q/color-mode :hsb)
  {:color 0
   :angle 0})



(q/defsketch my
             :host "my"
             :size [500 500]
             :setup setup
             :update nil
             ;;  :draw draw-state 10 10
             :middleware [m/fun-mode])