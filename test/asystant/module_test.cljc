(ns asystant.module-test
  (:require [asystant.module :refer :all]
            [clojure.test :refer :all]))

(deftest no-thru-edge-empty
  (empty? (:edges (subsystem []))))
