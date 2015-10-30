(ns asystant.pipe
  "Helper functions for creating pipe functions.

  A pipe function is a function of the form: (fn [in-ch out-ch]
                                               shutdown-callback)
  where the shutdown-callback is an optional 0-arg function that will
  be called when shutting down. If the connections made in the pipe function
  will be naturally closed when the in-ch closes, this is unnecessary.

  The purpose is to provide a flexible way of defining how to connect modules and
  these helpers allow you to perform simple connections without needing to worry about
  the details of core.async"
  (:require [clojure.core.async :as async]))

(defn sink
  "Create a pipe-function from a function that takes items and has no meaningful return value"
  [f]
  (fn [in-ch _]
    (async/go-loop []
      (when-some [v (async/<! in-ch)]
        (f v)
        (recur)))
    (fn [] nil)))

(defn source
  "Create a pipe-function from a function that takes a function that takes an item
   and has no meaningful return value"
  [f]
  (fn [_ out-ch]
    (let [shutdown-callback (f (fn [x] (async/put! out-ch x)))]
      (fn []
        (if (fn? shutdown-callback) (shutdown-callback))
        (async/close! out-ch)))))

(defn source-ch
  "Create a pipe-function from a channel that should act as a source"
  [src-ch]
  (fn [_ out-ch] (async/pipe src-ch out-ch)
    (fn [] (async/close! out-ch))))

(defn sink-ch
  "Create a pipe-function from a channel that should act as a sink"
  [ch]
  (fn [in-ch _] (async/pipe in-ch ch)))

(defn transform
  "Create a pipe-function that applies an x-form to items"
  [xf]
  (fn [in-ch out-ch] (async/pipe (async/pipe in-ch (async/chan 1 xf)) out-ch)
    (fn [] nil)))
