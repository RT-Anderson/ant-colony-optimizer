(ns ant-colony-optimizer.aco-visualizer
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]))

(defn setup []
  (q/frame-rate 1)                    ;; Set framerate to 1 FPS
  (q/background 200))

(defn draw
  ([waypoints colony]
   (q/stroke 255)             ;; Set the stroke colour to a random grey
   (q/stroke-weight 5)       ;; Set the stroke thickness randomly
   (q/fill 05)

   (let [diam 20             ;; Set the diameter to a value between 0 and 100
        x    (q/random (q/width))       ;; Set the x coord randomly within the sketch
        y    (q/random (q/height))     ;; Set the y coord randomly within the sketch
        path (->> colony :best-route deref :path)
        path2 (conj (subvec path 1) (first path))
        get-waypt (fn [index] (nth waypoints index))]

    (doall (map #(q/line (get-waypt %1) (get-waypt %2)) path path2))

    (q/stroke 255)             ;; Set the stroke colour to a random grey
    (q/stroke-weight 5)       ;; Set the stroke thickness randomly
    (q/fill 05)

    (doall (map #(q/ellipse (first %) (second %) diam diam) waypoints))))
  ([waypoints]
   (q/stroke 255)             ;; Set the stroke colour to a random grey
   (q/stroke-weight 5)       ;; Set the stroke thickness randomly
   (q/fill 05)
   (doall (map #(q/ellipse (first %) (second %) 20 20) waypoints))))


(defn sketch-waypoints [waypoints max-size]
  (q/defsketch example                  ;; Define a new sketch named example
             :title "Waypoint Map"    ;; Set the title of the sketch
             :settings #(q/smooth 2)             ;; Turn on anti-aliasing
             :setup setup                        ;; Specify the setup fn
             :draw #(draw waypoints)      ;; Specify the draw fn
             :size [max-size max-size]))

(defn sketch-route [waypoints colony max-size]
  (q/defsketch example                  ;; Define a new sketch named example
               :title "Ant Colony Optimization"    ;; Set the title of the sketch
               :settings #(q/smooth 2)             ;; Turn on anti-aliasing
               :setup setup                        ;; Specify the setup fn
               :draw #(draw waypoints colony)      ;; Specify the draw fn
               :size [max-size max-size]))