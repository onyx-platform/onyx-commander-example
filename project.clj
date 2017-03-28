(defproject onyx-commander-example "0.1.0-SNAPSHOT"
  :description "An example of using Onyx with the Commander pattern"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.onyxplatform/onyx "0.10.0-beta9"]
                 [org.onyxplatform/lib-onyx "0.10.0.0"]]
  :source-paths ["src"]
  :profiles {:dev {:jvm-opts ["-XX:-OmitStackTraceInFastThrow"]
                   :global-vars {*assert* true}
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [lein-project-version "0.1.0"]]}

             :uberjar {:aot [lib-onyx.media-driver
                             onyx-commander-example.core]
                       :uberjar-name "peer.jar"
                       :global-vars {*assert* false}}})
