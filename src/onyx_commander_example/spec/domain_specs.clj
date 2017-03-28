(ns onyx-commander-example.spec.domain-specs
  (:require [clojure.spec :as s]))

(s/def :transaction/id uuid?)

(s/def :account/id string?)

(s/def :account/from :account/id)

(s/def :account/to :account/id)

(s/def :account/amount integer?)
