(defproject onyx-commander-example "0.1.0-SNAPSHOT"
  :description "An example of using Onyx with the Commander pattern"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.onyxplatform/onyx "0.10.0-beta9"]
                 [org.onyxplatform/onyx-datomic "0.10.0.0-beta7"]
                 [org.onyxplatform/onyx-kafka "0.10.0.0-beta9"]
                 [org.onyxplatform/lib-onyx "0.10.0.0"]
                 [com.datomic/datomic-free "0.9.5407"
                  :exclusions [org.slf4j/log4j-over-slf4j
                               com.google.guava/guava
                               org.slf4j/slf4j-nop org.slf4j/slf4j-api]]
                 [org.apache.kafka/kafka_2.11 "0.10.1.1" :exclusions [org.slf4j/slf4j-log4j12]]
                 [io.weft/gregor "0.5.1"]]
  :source-paths ["src"]
  :profiles {:dev {:jvm-opts ["-XX:-OmitStackTraceInFastThrow"]
                   :global-vars {*assert* true}
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [lein-project-version "0.1.0"]]}

             :uberjar {:aot [lib-onyx.media-driver
                             onyx-commander-example.core]
                       :uberjar-name "peer.jar"
                       :global-vars {*assert* false}}})
