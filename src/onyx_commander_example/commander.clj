(ns onyx-commander-example.commander)

(def workflow
  [[:read-commands :process-commands]
   [:process-commands :update-materialized-view]])

(defn catalog [kafka-zookeeper commands-topic datomic-uri]
  [{:onyx/name :read-commands
    :onyx/type :input
    :onyx/medium :kafka
    :onyx/plugin :onyx.plugin.kafka/read-messages
    :onyx/max-peers 1
    :onyx/batch-size 50
    :kafka/zookeeper kafka-zookeeper
    :kafka/topic commands-topic
    :kafka/deserializer-fn :onyx-commander-example.impl/deserialize-kafka-message
    :kafka/offset-reset :earliest}

   {:onyx/name :process-commands
    :onyx/type :function
    :onyx/max-peers 1
    :onyx/batch-size 50
    :onyx/fn :clojure.core/identity}

   {:onyx/name :update-materialized-view
    :onyx/type :output
    :onyx/medium :datomic
    :onyx/plugin :onyx.plugin.datomic/write-bulk-datoms-async
    :onyx/max-peers 1
    :onyx/batch-size 50
    :datomic/uri datomic-uri
    :datomic/partition :example/commander}])

;; Only send segments to the last task if they are emitted from the trigger.
(def flow-conditions
  [{:flow/from :process-commands
    :flow/to [:update-materialized-view]
    :flow/short-circuit? true
    :flow/predicate :onyx-commander-example.impl/transaction?}])

(defn lifecycles [brokers event-topic]
  [{:lifecycle/task :read-commands
    :lifecycle/calls :onyx.plugin.kafka/read-messages-calls}

   {:lifecycle/task :update-materialized-view
    :lifecycle/calls :onyx.plugin.datomic/write-bulk-tx-async-calls}

   ;; Supply the Kafka broker and partition to send read receipts after
   ;; updating Datomic.
   {:lifecycle/task :update-materialized-view
    :lifecycle/calls :onyx-commander-example.impl/send-events
    :kafka/brokers brokers
    :commander/event-topic event-topic}])

;; The specified aggregation performs each of the commands and maintains
;; exactly-once semantics on the window.
(def windows
  [{:window/id :update-state
    :window/task :process-commands
    :window/type :global
    :window/aggregation :onyx-commander-example.impl/commands}])

;; Periodically send window data to the materialized view to be updated.
(def triggers
  [{:trigger/id :flush-state
    :trigger/window-id :update-state
    :trigger/refinement :onyx-commander-example.impl/discarding-events
    :trigger/on :onyx.triggers/segment
    :trigger/fire-all-extents? true
    :trigger/threshold [1 :element]
    :trigger/emit :onyx-commander-example.impl/transform-window-state}])
