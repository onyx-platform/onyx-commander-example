(ns onyx-commander-example.jobs.commander-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec :as s]
            [gregor.core :as g])
  (:import [java.util UUID]
           [org.apache.kafka.common.errors TopicExistsException]
           [org.apache.kafka.common.errors UnknownTopicOrPartitionException]
           [kafka.common TopicAlreadyMarkedForDeletionException]))

(def kafka-zookeeper "192.168.99.100:2181")

(def kafka-brokers "192.168.99.100:9092")

(def commands-topic "commands")

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

(deftest commander-test
  (testing "Test a sequence of commands"
    (delete-topic! kafka-zookeeper commands-topic)
    (make-topic! kafka-zookeeper commands-topic)
    (write-commands! kafka-brokers commands-topic commands)
    
    

    
    ))
