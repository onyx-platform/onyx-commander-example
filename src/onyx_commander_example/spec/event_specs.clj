(ns onyx-commander-example.spec.event-specs
  (:require [clojure.spec :as s]
            [onyx-commander-example.spec.base-specs :refer [event-action]]))

(s/def :event.account-created/data :command.create-account/data)

(s/def :event.money-deposited/data :command.deposit-money/data)

(s/def :event.money-withdrawn/data :command.withdraw-money/data)

(s/def :event.money-transferred/data :command.transfer-money/data)

(defmethod event-action :account-created
  [_]
  (s/keys :req [:event/id
                :event/parent-id
                :event/action
                :event/timestamp
                :event.account-created/data]))

(defmethod event-action :money-deposited
  [_]
  (s/keys :req [:event/id
                :event/parent-id
                :event/action
                :event/timestamp
                :event.money-deposited/data]))

(defmethod event-action :money-withdrawn
  [_]
  (s/keys :req [:event/id
                :event/parent-id
                :event/action
                :event/timestamp
                :event.money-withdrawn/data]))

(defmethod event-action :money-transferred
  [_]
  (s/keys :req [:event/id
                :event/parent-id
                :event/action
                :event/timestamp
                :event.money-transferred/data]))
