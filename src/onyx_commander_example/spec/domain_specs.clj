(ns onyx-commander-example.spec.domain-specs
  (:require [clojure.spec :as s]))

(s/def :transaction/id uuid?)

(s/def :account/from string?)

(s/def :account/to string?)

(s/def :account/amount integer?)
