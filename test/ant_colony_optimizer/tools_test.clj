(ns ant-colony-optimizer.tools-test
  (:require [ant-colony-optimizer.tools :refer :all])
  (:use midje.sweet))

(def mylist '([0 0] [3 4] [5 12] [12 44]))
(def myset [[0 0] [3 4] [5 12] [12 44]])

(fact "get-distance works for both lists and vectors"
      (get-distance 1 2 mylist) => 5
      (get-distance 1 2 myset)  => 5)

(fact "The Euclidean distance calculation is working"
      (def mylist '([0 0] [3 4] [5 12] [12 44]))
      (get-distance 1 2 mylist) => 5
      (get-distance 2 4 mylist) => 41
      (get-distance 1 3 mylist) => 13)

(fact "Exception thrown if selected node isn't in set"
      (get-distance 1 8 '([0 0] [0 0] [1 1])) => (throws Exception))

