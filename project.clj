(defproject asystant "0.1.0"
  :description "A simple, flexible helper for building modular systems with clojure/core.async"
  :url "https://github.com/monteithpj/asystant"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [aysylu/loom "0.5.4"]]
  :aliases
  {"repl" ["with-profile" "+test" "repl"]}
  :profiles
  {:test
   {:dependencies [[org.clojure/test.check "0.8.2"]
                   [com.gfredericks/test.chuck "0.2.0"]
                   [com.velisco/herbert "0.7.0-alpha1"]]}}
  :plugins [[codox "0.8.15"]]
  :codox {:src-dir-uri "https://github.com/monteithpj/asystant/blob/master"})
