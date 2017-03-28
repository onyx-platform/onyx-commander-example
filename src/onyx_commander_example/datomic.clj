(ns onyx-commander-example.datomic
  (:require [datomic.api :as d]))

(def schema
  [{:db/id (d/tempid :db.part/db)
    :db/ident :example/commander
    :db.install/_partition :db.part/db}

   {:db/id (d/tempid :db.part/db)
    :db/ident :account/id
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}

   {:db/id (d/tempid :db.part/db)
    :db/ident :account/balance
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}])
