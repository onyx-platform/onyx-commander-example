(ns onyx-commander-example.spec.command-specs
  (:require [clojure.spec :as s]
            [onyx-commander-example.spec.base-specs :refer [command-action]]))

(s/def :account/from string?)

(s/def :account/to string?)

(s/def :account/amount integer?)

(s/def :command.deposit-money/data
  (s/keys :req [:account/to :account/amount]))

(s/def :command.withdraw-money/data
  (s/keys :req [:account/from :account/amount]))

(s/def :command.transfer-money/data
  (s/keys :req [:account/from :account/to :account/amount]))

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
