(defproject ant-colony-optimizer "0.1.0"
  :description "Ant Colony Optimization Algorithm Written in Clojure"
  :url "https://github.com/RT-Anderson/ant-colony-optimizer"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [quil "2.3.0"]]


  :profiles {:dev {:dependencies [[midje "1.8.2"]]
                   :plugins [[lein-midje "3.1.3"]]}
             :uberjar {:aot :all}})
