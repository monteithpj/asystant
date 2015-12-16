(ns asystant.visualise
  (:require [loom.graph :as lgraph]
            [loom.label :as llabel]
            [clojure.core.match :as match]))

(defn add-nodes [graph nodes]
  (apply llabel/add-labeled-nodes graph (interleave nodes (map :name nodes))))

(defn add-edges [g edges]
  (reduce (fn [graph [[from to :as edge] edges]]
            (-> graph
                (lgraph/add-edges edge)
                ;;(llabel/add-label from to (into [] (map :type) edges))
                ))
          g
          (group-by (fn [{:keys [from to]}] [from to]) edges)))

(defn system->loomgraph [sys]
  (-> (lgraph/digraph)
      (add-nodes (:nodes sys))
      (add-edges (:edges sys))))

(def simplify-graph-xf
  (comp (map
         (fn [node]
           (assoc node :name
                  (match/match [(:ins node) (:outs node)]
                               [:any :any] (str "*"(:name node)"*")
                               [_    :any] (str (:name node) "*")
                               [:any _]    (str "*" (:name node))
                               :else       (:name node)))))
        (map (fn [node] (let [strip-any (fn [topics]
                                          (if (= :any topics) #{} topics))]
                          (-> node
                              (update :ins strip-any)
                              (update :outs strip-any)))))))

(def remove-unneeded-xf
  (map (fn [node] (select-keys node [:ins :outs]))))
