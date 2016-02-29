# asystant

A simple, flexible helper for building modular systems with clojure/core.async

asystant aims to maximise decoupling of modules through an event-based architecture, using data dependencies rather than component dependencies to assemble the system. In addition, using core.async and a single, unifying interface decouples the overall system architecture from the execution model of individual modules.

You can also watch a [talk](https://skillsmatter.com/skillscasts/7257-building-modular-systems-with-asystant) given at [Clojure eXchange 2015](https://skillsmatter.com/conferences/6861-clojure-exchange-2015) which explains in more detail some motivation and how asystant works

###0.1.2 Changelog

* ClojureScript support
* Minor improvements to subsystem assembly

## Usage

[![Clojars Project](http://clojars.org/asystant/latest-version.svg)](http://clojars.org/asystant)

Declare modules of the form: 
```clojure
{:ins #{topics} :outs #{topics} :pipe pipe-fn-creator}
```

Connect together your system design: 
```clojure
(asystant.core/add-modules asystant.core/new-system modules)
```

Build your system:
```clojure
(asystant.build/build! system initialisation-params)
```

The pipe function creator takes a map of initialisation parameters and returns a pipe function.
```clojure
(defn my-pipe-fn-creator [{:keys [bar]}]
  (fn [in-ch out-ch]
    (foo bar in-ch out-ch)))
```

A pipe function is a function that takes an input channel and an output channel. This will be called on each module when you call build! and should be used to connect your functionality to the rest of the system.

```clojure
(defn my-pipe-fn [in-ch out-ch]
  (clojure.core.async/go-loop []
    (println (clojure.core.async/<! in-ch))
    (recur)))
```

Pipe utilities are available for the most common uses (source, sink, transform), allowing most modules to avoid directly using core.async at all. The previous example could be rewritten:

```clojure
(asystant.pipe/sink (fn [x] (println x)))
```

There is currently some basic system visualisation functionality (using loom and graphviz) which will be built on, allowing easy debugging of the system making it simple to identify problems

## License

Copyright © 2015 Patrick Monteith

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
