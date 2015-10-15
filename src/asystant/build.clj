(ns asystant.build
  (:require [clojure.core.async :as async]
            [asystant.buffers :refer [create-buffer]]))

(defn out-chan
  "Create a channel that will emit the correct topics to all channels that come from the given module"
  [edge-chans module]
  (let [out-ch (async/chan)
        out-mult (async/mult out-ch)]
    (transduce (filter (comp (partial = module) :from))
               (completing (fn [pub-ch {:keys [chan type]}]
                             (if (= :any type)
                               (async/tap out-mult chan)
                               (async/sub pub-ch type chan))
                             pub-ch))
               (async/pub (async/tap out-mult (async/chan)) :type)
               edge-chans)
    out-ch))

(defn in-chan
  [edge-chans module]
  (let [ins (into [] (comp (filter (comp (partial = module) :to))
                           (map :chan))
                  edge-chans)]
    (if-let [transform (-> module :transforms :all)]
      (async/pipe (async/merge ins) (async/chan (-> module :buffers :all create-buffer)
                                                transform))
      (async/merge ins (-> module :buffers :all create-buffer)))))

(defn edge->buffer [edge]
  (-> edge :to :buffers (get (:type edge)) create-buffer))

(defn edge->transform [edge]
  (-> edge :to :transforms (get (:type edge))))

(defn make-edge-chans
  "Take a set of edges and associate a channel to each one"
  [edges]
  (map (fn [edge] (assoc edge :chan (async/chan (edge->buffer edge)
                                                (edge->transform edge)))) edges))

(defn attach-module
  "Returns a reducing function that will take a collection of shutdown callbacks and a module
   and will connect that module to a functional system in the same shape of the given system.
   Returns a new collection of shutdown callbacks."
  [{:keys [edges]} initialisation-params]
  (let [edge-chans (make-edge-chans edges)]
    (fn [callbacks module]
      (conj callbacks (((:pipe module) initialisation-params)
                       (in-chan  edge-chans module)
                       (out-chan edge-chans module))))))

(defn build!
  "Take a graph system with the structure {:nodes #{{:pipe pipe-fn}...} 
                                           :edges #{{:from from-node :to to-node}...}}
   where the pipe-fn is a function (fn [initialisation-params] 
                                     (fn [in-ch out-ch] shutdown-callback))
   Return a function that will invoke all of the shutdown-callbacks returned from the modules'
   pipe-fns"
  [sysdef initialisation-params]
  (let [shutdown-callbacks (filter fn? (reduce (attach-module sysdef initialisation-params)
                                               [] (:nodes sysdef)))]
    (fn [] (doseq [f shutdown-callbacks] (f)))))

(defn make-connections [edge-chans]
  (fn [module]
    (-> module
        (assoc :in-chan (in-chan edge-chans module))
        (assoc :out-chan (out-chan edge-chans module)))))

(defn build2!
  [sysdef init-p]
  (let [edge-chans (make-edge-chans (:edges sysdef))
        connected (map (make-connections edge-chans) (:nodes sysdef))
        shutdown-callbacks
        (reduce (fn [callbacks module] (conj callbacks (((:pipe module) init-p)
                                                        (:in-chan module)
                                                        (:out-chan module))))
                [] connected)]
    (fn [] (doseq [f shutdown-callbacks] (f)))))
