(ns asystant.pipe-test
  (:require [asystant.pipe :refer :all]
            [clojure.test :refer :all]
            [clojure.core.async :as async]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [com.gfredericks.test.chuck.properties :as cprop]
            [asystant.test.util.events :as events]))

(defspec source-test
  (prop/for-all [messages events/gen-events]
                (let [callback (atom nil)
                      src-pipe (source (fn [f] (reset! callback f) nil))
                      out-ch   (async/chan)
                      sdc      (src-pipe nil out-ch)]
                  (doseq [m messages] (@callback m))
                  (sdc)
                  (is (= messages
                         (async/<!! (async/into [] out-ch)))))))

(defspec source-ch-test
  (prop/for-all [messages events/gen-events]
                (let [src-ch (async/chan)
                      out-ch (async/chan)
                      res-ch (async/into [] out-ch)
                      sdc    ((source-ch src-ch) nil out-ch)]
                  (async/<!! (async/onto-chan src-ch messages))
                  (sdc)
                  (is (= messages
                         (async/<!! res-ch))))))

(defspec sink-test
  (prop/for-all [messages events/gen-events]
                (let [output (atom [])
                      sink-fn (fn [m] (swap! output conj m))
                      in-ch   (async/chan)
                      sdc     ((sink sink-fn) in-ch nil)]
                  (async/<!! (async/onto-chan in-ch messages))
                  (sdc)
                  (is (= messages @output)))))

(defspec transform-test
  (prop/for-all [messages events/gen-events]
                (let [out-ch (async/chan)
                      in-ch  (async/chan)
                      res-ch (async/into [] out-ch)
                      xf     (map (fn [m] (assoc m :test :transform)))
                      sdc    ((transform xf) in-ch out-ch)]
                  (async/<!! (async/onto-chan in-ch messages))
                  (sdc)
                  (is (= (into [] xf messages)
                         (async/<!! res-ch))))))
