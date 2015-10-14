(ns asystant.core-test
  (:require [clojure.test :refer :all]
            [asystant.core :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [miner.herbert.generators :as hg]))

(defn apply-set [module]
  (-> module
      (update :ins set)
      (update :outs set)))

(def module-schema '{:ins #{kw*} :outs #{kw*}})
(def gen-module (gen/fmap apply-set (hg/generator '{:ins  (vec :a? :b? :c? :d? :e?)
                                                    :outs (vec :a? :b? :c? :d? :e?)})))

(defspec all-modules-added-are-in-system
  (prop/for-all [modules (gen/vector gen-module)]
                (= (set modules)
                   (:nodes (add-modules new-system modules)))))

(defspec all-edges-are-correct
  (prop/for-all [modules (gen/vector gen-module)]
                (every? (fn [{:keys [from to type] :as edge}]
                          (and ((:outs from)  type)
                               ((:ins to)     type)))
                        (:edges (add-modules new-system modules)))))
