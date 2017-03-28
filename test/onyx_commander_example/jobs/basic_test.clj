(ns onyx-commander-example.jobs.basic-test
  (:require [clojure.core.async :refer [>!! close!]]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [clojure.spec :as s]
            [onyx api
             [test-helper :refer [with-test-env]]]
            [onyx.plugin.core-async :refer [get-core-async-channels take-segments!]]
            onyx-commander-example.jobs.basic
            onyx-commander-example.tasks.math
            onyx.tasks.core-async)
  (:import [java.util UUID]))

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

(deftest commander-test
  (testing "Test a sequence of commands"
    (doseq [command commands]
      (when-let [errors (s/explain-data :commander/command command)]
        (throw (ex-info "Command failed to conform to spec." {:errors errors}))))))
