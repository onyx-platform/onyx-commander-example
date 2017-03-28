(ns onyx-commander-example.jobs.commander-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec :as s]
            [onyx-commander-example.commander :as c]
            [onyx-commander-example.datomic :as da]
            [onyx-commander-example.spec.base-specs]
            [onyx-commander-example.spec.domain-specs]
            [onyx-commander-example.spec.command-specs]
            [onyx-commander-example.spec.event-specs]
            [onyx-commander-example.impl]
            [datomic.api :as d]
            [gregor.core :as g]
            [onyx.test-helper :refer [with-test-env load-config]]
            [onyx.plugin.datomic]
            [onyx.plugin.kafka]
            [onyx.api])
  (:import [java.util UUID]
           [org.apache.kafka.common.errors TopicExistsException]
           [org.apache.kafka.common.errors UnknownTopicOrPartitionException]
           [kafka.common TopicAlreadyMarkedForDeletionException]))

(def kafka-zookeeper "192.168.99.100:2181")

(def kafka-brokers "192.168.99.100:9092")

(def commands-topic "commands")

(def events-topic "events")

(defn now []
  (java.util.Date.))

(def commands
  [{:command/id (UUID/randomUUID)
    :command/action :create-account
    :command/timestamp (now)
    :command.create-account/data {:account/id "123"}}

   {:command/id (UUID/randomUUID)
    :command/action :deposit-money
    :command/timestamp (now)
    :command.deposit-money/data {:account/to "123"
                                 :account/amount 50}}

   {:command/id (UUID/randomUUID)
    :command/action :withdraw-money
    :command/timestamp (now)
    :command.withdraw-money/data {:account/from "123"
                                  :account/amount 30}}

   {:command/id (UUID/randomUUID)
    :command/action :deposit-money
    :command/timestamp (now)
    :command.deposit-money/data {:account/to "123"
                                 :account/amount 70}}])

(defn make-topic! [zk topic-name]
  (try
    (g/create-topic {:connection-string zk} topic-name {})
    (catch TopicExistsException e)))

(defn delete-topic! [zk topic-name]
  (try
    (g/delete-topic {:connection-string zk} topic-name)
    (catch TopicAlreadyMarkedForDeletionException e)
    (catch UnknownTopicOrPartitionException e)))

(defn write-commands! [brokers topic command-sequence]
  (doseq [command command-sequence]
    (when-let [errors (s/explain-data :commander/command command)]
      (throw (ex-info "Command failed to conform to spec." {:errors errors}))))

  (with-open [p (g/producer brokers)]
    (let [command command-sequence]
      (g/send p topic topic (pr-str command)))))

(defn create-datomic-db! [uri schema]
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (d/transact conn schema)
    conn))

(defn initialize-kafka! []
  (delete-topic! kafka-zookeeper commands-topic)
  (delete-topic! kafka-zookeeper events-topic)

  (make-topic! kafka-zookeeper commands-topic)
  (make-topic! kafka-zookeeper events-topic)

  (write-commands! kafka-brokers commands-topic commands))

;; Run me once.
;; (initialize-kafka!)

(deftest commander-test
  (testing "Test a sequence of commands"
    (let [datomic-uri (str "datomic:mem://" (java.util.UUID/randomUUID))
          datomic-conn (create-datomic-db! datomic-uri da/schema)
          tenancy-id (UUID/randomUUID)
          config (load-config "dev-config.edn")
          env-config (assoc (:env-config config) :onyx/tenancy-id tenancy-id)
          peer-config (assoc (:peer-config config) :onyx/tenancy-id tenancy-id)
          job {:workflow c/workflow
               :catalog (c/catalog kafka-zookeeper commands-topic datomic-uri)
               :flow-conditions c/flow-conditions
               :lifecycles c/lifecycles
               :windows c/windows
               :triggers c/triggers
               :task-scheduler :onyx.task-scheduler/balanced}]
      (with-test-env [test-env [3 env-config peer-config]]
        (clojure.pprint/pprint (onyx.api/submit-job peer-config job))
        (Thread/sleep 4000)
        (prn (d/pull (d/db datomic-conn) [:account/id :account/balance] [:account/id "123"]))))))
