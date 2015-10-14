(ns asystant.core
  (:require [clojure.set :refer [intersection]]
            [clojure.core.match :as match]))

(def new-system
  "A system with no modules"
  {:nodes #{} :edges #{}})

(defn match-types
  "Given a from-module and to-module, return the set of topics that
   are members of the outs of the from-module and the ins of the to-module.
   If the from-module emits :any and the to-module accepts :any, return #{:any}"
  [{:keys [ins]} {:keys [outs]}]
  (match/match [ins outs]
               [:any :any] #{:any}
               [:any  _  ] outs
               [ _   :any] ins
               :else (intersection ins outs)))

(defn from-edges
  "Returns an x-form that will transform another module into 
   the edges that go from the given module to the other module"
  [module]
  (mapcat (fn [other-mod] (map (fn [type] {:from module :to other-mod :type type})
                               (match-types other-mod module)))))

(defn to-edges
  "Returns an x-form that will transform another module into
   the edges that go from the other module to the given module"
  [module]
  (mapcat (fn [other-mod] (map (fn [type] {:from other-mod :to module :type type})
                               (match-types module other-mod)))))

(defn make-edges
  "Create all the edges that connect the given module to the system"
  [{:keys [nodes]} module]
  (mapcat (fn [xf] (into [] xf nodes))
          ((juxt from-edges to-edges) module)))

(defn add-module
  "Add a module to the system, creating edges between this module and other modules as appropriate.
   Modules are maps of the form {:ins #{...} :outs #{...}} where the set of ins are the input topics
   and the set of outs are output topics"
  [sysdef module]
  (-> sysdef
      (update :nodes conj module)
      (update :edges into (make-edges sysdef module))))

(defn add-modules
  [sysdef modules]
  (reduce add-module sysdef modules))

(defn with-transform
  "Recreate the system, applying the given x-form to the nodes of the system"
  [sysdef xf]
  (transduce xf (completing add-module) new-system (:nodes sysdef)))
