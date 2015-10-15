(ns asystant.buffers
  "Buffer creation. Defines the multimethod create-buffer that accepts a tuple [type size]
   which can be added to with defmethod for custom buffer types. For ease of use, calling 
   create-buffer with just a size will create a fixed size buffer of that size."
  (:require [clojure.core.async :as async]))

(defmulti create-buffer (fn [buffer-def] (cond
                                           (nil? buffer-def)     nil
                                           (number? buffer-def) :fixed
                                           :else                (first buffer-def))))

(defmethod create-buffer nil [_] nil)

(defmethod create-buffer :fixed [size] size)

(defmethod create-buffer :sliding-buffer [[_ size]] (async/sliding-buffer size))

(defmethod create-buffer :dropping-buffer [[_ size]] (async/dropping-buffer size))
