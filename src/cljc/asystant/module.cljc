(ns asystant.module
  (:require [asystant.core :as asys]
            [asystant.build :as build]
            [asystant.pipe  :as pipe]))

(comment (def log-node-xf
           (map (fn [node]
                  (update node :pipe (fn [pipe-init]
                                       (comp (pu/logged-pipe (:name node)) pipe-init)))))))

(defn remove-thru-edge
  [graph]
  (update graph :edges
          (partial into #{} (remove
                             (fn [e] (and (-> e :from :in-pipe)
                                          (-> e :to :out-pipe)))))))

(defn system->module
  ([ins outs sys initialisation-params-updater]
   (system->module ins outs sys initialisation-params-updater {}))
  ([ins outs sys initialisation-params-updater extras]
   (conj {:ins  ins
          :outs outs
          :pipe (fn [initialisation-params]
                  (fn [in-ch out-ch]
                    (let [updated-params
                          (initialisation-params-updater initialisation-params)
                          in-pipe {:ins #{}
                                   :outs ins
                                   :pipe (constantly (pipe/source-ch in-ch))
                                   :name (str (:name extras) "-in-pipe")
                                   :in-pipe true}
                          out-pipe {:ins outs
                                    :outs #{}
                                    :pipe (constantly (pipe/sink-ch out-ch))
                                    :name (str (:name extras) "-out-pipe")
                                    :out-pipe true}
                          graph (remove-thru-edge
                                 (asys/add-modules sys [in-pipe out-pipe]))
                          shutdown (build/build! graph
                                                 (:params updated-params))]
                      (fn []
                        (or (:callback updated-params) identity)
                        (shutdown)))))
          :subsystem sys}
         extras)))

(defn merge-topics [topic-sets]
  (reduce (fn [merged topic-set]
            (if (= :any topic-set)
              (reduced :any)
              (into merged topic-set)))
          #{}
          topic-sets))

(defn subsystem
  "Take a collection of modules and create a single module from them.
   The ins and outs will be the merged ins and merged outs from the sub-modules

   The subsystem itself will be included on the module under the key :subsystem,
   allowing the module to be expanded out, should that be desired.

   Optionally takes a map of extra params which will be included on the resulting module

   You can include a :subsystem-init function in extras which takes the outer-system's
   initialisation params and returns a map: {:params params-updater
                                             :callback shutdown-callback}

   params-updater: A function that takes the initialisation params and returns
   a modified version of them that will be provided to all sub-modules

   shutdown-callback: A 0-arg function that will be called when the system is shutting
   down. This will be combined with any shutdown callbacks from sub-modules and returned
   from the module's pipe-function"
  ([modules]
   (subsystem modules {}))
  ([modules extras]
   (system->module (merge-topics (map :ins modules))
                   (merge-topics (map :outs modules))
                   (asys/add-modules asys/new-system modules)
                   (or (:subsystem-init extras) (partial assoc {} :params))
                   extras)))

(def expand-subsystems-xf
  "For use with system-graph/with-transform"
  (mapcat (fn [node] (if-let [subsystem (:subsystem node)]
                       (:nodes subsystem)
                       node))))
