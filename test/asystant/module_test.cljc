(ns asystant.module-test
  (:require [asystant.module :refer :all]
            [clojure.test :refer :all]))

(deftest no-thru-edge-any-any
  (empty? (:edges (subsystem [{:ins :any} {:outs :any}]))))
