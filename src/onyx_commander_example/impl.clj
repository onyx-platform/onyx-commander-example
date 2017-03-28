(ns onyx-commander-example.impl
  (:require [clojure.spec :as s]
            [onyx.windowing.aggregation :refer [set-value-aggregation-apply-log]]
            [datomic.api :as d]
            [gregor.core :as g])
  (:import [java.util UUID]))

(defn now []
  (java.util.Date.))

(defn init [window]
  {:accounts {}
   :events []})

(defn create-account [{:keys [:command.create-account/data] :as segment} state]
  (let [{:keys [account/id]} data]
    (-> state
        (assoc-in [:accounts id :balance] 0)
        (update :events conj {:event/id (UUID/randomUUID)
                              :event/parent-id (:command/id segment)
                              :event/action :account-created
                              :event/timestamp (now)
                              :event.account-created/data data}))))

(defn deposit-money [{:keys [:command.deposit-money/data] :as segment} state]
  (let [{:keys [account/to account/amount]} data]
    (-> state
        (update-in [:accounts to :balance] + amount)
        (update :events conj {:event/id (UUID/randomUUID)
                              :event/parent-id (:command/id segment)
                              :event/action :money-deposited
                              :event/timestamp (now)
                              :event.money-deposited/data data}))))

(defn withdraw-money [{:keys [:command.withdraw-money/data] :as segment} state]
  (let [{:keys [account/from account/amount]} data]
    (-> state
        (update-in [:accounts from :balance] - amount)
        (update :events conj {:event/id (UUID/randomUUID)
                              :event/parent-id (:command/id segment)
                              :event/action :money-withdrawn
                              :event/timestamp (now)
                              :event.money-withdrawn/data data}))))

(defn transfer-money [{:keys [:command.transfer-money/data] :as segment} state]
  (let [{:keys [account/from account/to account/amount]} data]
    (-> state
        (update-in [:accounts from :balance] - amount)
        (update-in [:accounts to :balance] + amount)
        (update :events conj {:event/id (UUID/randomUUID)
                              :event/parent-id (:command/id segment)
                              :event/action :money-transferred
                              :event/timestamp (now)
                              :event.money-transferred/data data}))))

(defn aggregation [window state segment]
  (condp = (:command/action segment)
    :create-account (create-account segment state)
    :deposit-money (deposit-money segment state) 
    :withdraw-money (withdraw-money segment state)
    :transfer-money (transfer-money segment state)))

(defn super-aggregation [window state-1 state-2]
  (reduce-kv
   (fn [result k v]
     (if (get result k)
       (update-in result [k :balance] + (:balance v))
       (assoc result k v)))
   state-1
   state-2))

(def commands
  {:aggregation/init init
   :aggregation/create-state-update aggregation
   :aggregation/apply-state-update set-value-aggregation-apply-log
   :aggregation/super-aggregation-fn super-aggregation})

(defn discarding-events-create-state-update [trigger state state-event])

(defn discarding-events-apply-state-update [trigger state entry]
  (dissoc state :events))

(def discarding-events
  {:refinement/create-state-update discarding-events-create-state-update 
   :refinement/apply-state-update discarding-events-apply-state-update})

(defn transform-window-state [event window trigger state-event state]
  (doseq [event (:events state)]
    (when-let [errors (s/explain-data :commander/event event)]
      (throw (ex-info "Event failed to conform to spec." {:errors errors}))))

  {:tx
   (reduce-kv
    (fn [result account-id {:keys [balance]}]
      (conj result {:db/id (d/tempid :db.part/user)
                    :account/id account-id
                    :account/balance balance}))
    []
    (:accounts state))
   :events (:events state)})

(defn deserialize-kafka-message [bytes]
  (read-string (String. bytes "UTF-8")))

(defn transaction? [event old new all-new]
  (contains? new :tx))

(defn initialize-producer [event lifecycle]
  (let [producer (g/producer (:kafka/brokers lifecycle))]
    {:commander/producer producer}))

(defn forward-events [event lifecycle]
  (let [p (:commander/producer event)
        events (->> (get-in event [:onyx.core/batch])
                    (map :events)
                    (reduce into []))]
    (doseq [event events]
      (g/send p (:commander/event-topic lifecycle) (pr-str event))))
  {})

(defn close-producer [event lifecycle]
  (.close (:commander/producer event))
  {})

(def send-events
  {:lifecycle/before-task-start initialize-producer
   :lifecycle/after-batch forward-events
   :lifecycle/after-task-stop close-producer})
