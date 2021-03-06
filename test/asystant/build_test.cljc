(ns asystant.build-test
  (:require [asystant.build :refer :all]
            [asystant.core :refer :all]
            [asystant.test.util.generators :as ugen]
            [asystant.test.util.events :as events]
            [clojure.test.check.generators :as gen]
            [com.gfredericks.test.chuck.generators :as cgen]
            [com.gfredericks.test.chuck.properties :as cprop]
            #?@(:clj  [[clojure.test :refer :all]
                      [clojure.core.async :as async]
                      [clojure.test.check.clojure-test :refer [defspec]]]
               :cljs [[cljs.test :refer :all]
                      [cljs.core.async :as async]
                      [cljs.test.check.clojure-test :refer-macros [defspec]]])))

(def gen-source
  (cgen/for [outs events/gen-topics
             output (events/topics->gen-events outs)]
    {:ins #{}
     :outs outs
     :pipe (fn [_] (fn [_ out-ch] (async/onto-chan out-ch output)))
     :output output
     ::type ::src}))

(defn sink-module
  [ins]
  (let [id (keyword (gensym))]
    {:ins ins
     :outs #{}
     :pipe (fn [{:keys [sinks]}] (fn [in-ch _] (async/pipe (async/into [] in-ch) (get sinks id))))
     :id id
     ::type ::sink}))

(def gen-sink
  (gen/fmap sink-module events/gen-topics))

(defn make-sink-chans [modules]
  (reduce (fn [sink-chans module]
            (if (= ::sink (::type module))
              (assoc sink-chans (:id module) (async/chan))
              sink-chans))
          {}
          modules))

(defn extract-input [sinks]
  (fn [module]
    (when-let [sink-ch (get sinks (:id module))]
      (assoc module :input (async/<!! sink-ch)))))

(comment "Spin up random systems consisting of sinks and sources ensuring that all the output
 from the sources is received by the sinks. Sinks will not produce results until their channels
 are closed, so this also checks for correct shutdown behaviour")
(defspec test-simple-system
  (cprop/for-all [modules (gen/vector (gen/one-of [gen-source gen-sink]))
                  :let [system      (add-modules new-system modules)
                        output      (into [] (mapcat :output) modules)
                        output-freq (frequencies (map :type output))
                        sinks       (make-sink-chans modules)
                        shutdown    (build! system {:sinks sinks})
                        extracted   (keep (extract-input sinks) modules)]]
                 (every? (fn [{:keys [ins input]}]
                           (= (select-keys output-freq ins) (frequencies (map :type input))))
                         extracted)))
