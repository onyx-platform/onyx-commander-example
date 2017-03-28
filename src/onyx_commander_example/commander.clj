(ns onyx-commander-example.commander)

(def workflow
  [[:read-commands :process-commands]
   [:process-commands :update-materialized-view]])

(defn catalog [kafka-zookeeper commands-topic]
  [{:onyx/name :read-commands
    :onyx/type :input
    :onyx/medium :kafka
    :onyx/max-peers 1
    :onyx/batch-size 50
    :kafka/zookeeper kafka-zookeeper
    :kafka/topic commands-topic}

   {:onyx/name :process-commands
    :onyx/type :function
    :onyx/max-peers 1
    :onyx/batch-size 50
    :onyx/fn :clojure.core/identity}

   {:onyx/name :update-materialized-view
    :onyx/type :output
    :onyx/medium :datomic
    :onyx/max-peers 1
    :onyx/batch-size 50}])

(def aggregations
  [{:window/id :update-state
    :window/task :process-commands
    :window/type :global
    :window/aggregation :onyx-commander-example.impl/commands}])
