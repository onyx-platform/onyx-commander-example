(ns onyx-commander-example.spec.base-specs
  (:require [clojure.spec :as s]))

(s/def :command/id uuid?)

(s/def :command/action keyword?)

(s/def :command/data map?)

(s/def :command/timestamp inst?)

(defmulti command-action :command/action)

(s/def :commander/command (s/multi-spec command-action :command/action))

(s/def :event/id uuid?)

(s/def :event/parent-id uuid?)

(s/def :event/action keyword?)

(s/def :event/data map?)

(s/def :event/timestamp inst?)

(defmulti event-action :event/action)

(s/def :commander/event (s/multi-spec event-action :event/action))
