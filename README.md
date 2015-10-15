# asystant

A simple, flexible helper for building modular systems with clojure/core.async

## Usage

[![Clojars Project](http://clojars.org/asystant/latest-version.svg)](http://clojars.org/asystant)

Declare modules of the form {:ins #{topics} :outs #{topics} :pipe pipe-fn-creator*}

Connect together your system design (asystant.core/add-modules asystant.core/new-system modules)

Build your system (asystant.build/build! system initialisation-params)

* The pipe function creator takes a map of initialisation parameters and returns a pipe function.

A pipe function is a function that takes an input channel and an output channel. This will be called on each module when you call build! and should be used to connect your functionality to the rest of the system.

Pipe utilities are available for the most common uses (source, sink, transform), allowing most modules to avoid directly using core.async at all

There is currently some basic system visualisation functionality (using loom and graphviz) which will be built on, allowing easy debugging of the system making it simple to identify problems

## License

Copyright © 2015 Patrick Monteith

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
