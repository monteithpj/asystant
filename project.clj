(defproject asystant "0.1.2"
  :description "A simple, flexible helper for building modular systems with clojure/core.async"
  :url "https://github.com/monteithpj/asystant"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/core.async "0.2.374"]
                 [org.clojure/core.match "0.3.0-alpha4"]]

  :source-paths ["src/cljc"]

  :release-tasks    [["vcs" "assert-committed"]
                     ["change" "version" "leiningen.release/bump-version" "release"]
                     ["vcs" "commit"]
                     ["vcs" "tag"]
                     ["change" "version" "leiningen.release/bump-version"]
                     ["vcs" "commit"]
                     ["vcs" "push"]]

  :aliases
  {"repl" ["with-profile" "+test" "repl"]}
  :profiles
  {:test
   {:dependencies [[org.clojure/test.check "0.8.2"]
                   [com.gfredericks/test.chuck "0.2.0"]
                   [com.velisco/herbert "0.7.0-alpha1"]
                   [aysylu/loom "0.5.4"]]}
   :deploy
   {:codox {:output-path "doc"}}}
  :plugins [[lein-codox "0.9.0"]
            [lein-cljsbuild "1.1.1"]]
  :codox {:source-uri "https://github.com/monteithpj/asystant/blob/master/{filepath}#L{line}"}
  :cljsbuild {:builds [{
                        :source-paths ["src/cljc"]
                        :compiler {:output-to "target/js/main.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]}
  :hooks [leiningen.cljsbuild])
