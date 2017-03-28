(ns onyx-commander-example.spec.command-specs
  (:require [clojure.spec :as s]
            [onyx-commander-example.spec.base-specs :refer [command-action]]))

(s/def :command.create-account/data
  (s/keys :req [:account/id]))

(s/def :command.deposit-money/data
  (s/keys :req [:account/to :account/amount]))

(s/def :command.withdraw-money/data
  (s/keys :req [:account/from :account/amount]))

(s/def :command.transfer-money/data
  (s/keys :req [:account/from :account/to :account/amount]))

(defmethod command-action :create-account
  [_]
  (s/keys :req [:command/id
                :command/action
                :command/timestamp
                :command.create-account/data]))

(defmethod command-action :deposit-money
  [_]
  (s/keys :req [:command/id
                :command/action
                :command/timestamp
                :command.deposit-money/data]))

(defmethod command-action :withdraw-money
  [_]
  (s/keys :req [:command/id
                :command/action
                :command/timestamp
                :command.withdraw-money/data]))

(defmethod command-action :transfer-money
  [_]
  (s/keys :req [:command/id
                :command/action
                :command/timestamp
                :command.transfer-money/data]))
