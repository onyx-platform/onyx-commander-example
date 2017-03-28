(ns onyx-commander-example.spec.event-specs
  (:require [clojure.spec :as s]
            [onyx-commander-example.spec.base-specs :refer [event-action]]))

(s/def :event.money-transferred/data
  (s/keys :req [:transaction/id :account/from :account/to :account/amount]))

(defmethod event-action :money-transferred
  [_]
  (s/keys :req [:event/id
                :event/parent-id
                :event/action
                :event/timestamp
                :event.money-transferred/data]))
