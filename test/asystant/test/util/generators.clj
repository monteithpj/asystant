(ns asystant.test.util.generators
  (:require [clojure.test.check.generators :as gen]
            [com.gfredericks.test.chuck.generators :as cgen]))

(defn optional
  "Generate a value from the given generator or nil"
  [generator]
  (gen/frequency [[19 generator]
                  [1 (gen/return nil)]]))

(defn set
  "Generate subsets of the given collection"
  [coll]
  (let [set-coll (clojure.core/set coll)]
    (cgen/for [size (gen/choose 0 (count set-coll))
               perm (gen/shuffle set-coll)]
      (clojure.core/set (take size perm)))))
