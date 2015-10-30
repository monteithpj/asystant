(ns asystant.test.util.events
  (:require [clojure.test.check.generators :as gen]
            [asystant.test.util.generators :as ugen]))

(defn dummy-event
  [topic]
  {:type topic})

(def topics [:a :b :c :d :e])

(def gen-topics (gen/not-empty (ugen/set topics)))

(defn topics->gen-events
  [topics]
  (gen/not-empty (gen/vector (gen/fmap dummy-event (gen/elements topics)))))

(def gen-events
  (topics->gen-events topics))
