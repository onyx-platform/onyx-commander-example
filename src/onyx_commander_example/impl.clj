(ns onyx-commander-example.impl
  (:require [onyx.windowing.aggregation :refer [set-value-aggregation-apply-log]]))

(defn init [window]
  {})

(defn create-account [{:keys [:command.create-account/data] :as segment} state]
  (let [{:keys [account/id]} data]
    (assoc-in state [id :balance] 0)))

(defn deposit-money [{:keys [:command.deposit-money/data] :as segment} state]
  (let [{:keys [account/to account/amount]} data]
    (update-in state [to :balance] + amount)))

(defn withdraw-money [{:keys [:command.withdraw-money/data] :as segment} state]
  (let [{:keys [account/from account/amount]} data]
    (update-in state [from :balance] - amount)))

(defn transfer-money [{:keys [:command.transfer-money/data] :as segment} state]
  (let [{:keys [account/from account/to account/amount]} data]
    (-> state
        (update-in state [from :balance] - amount)
        (update-in state [to :balance] + amount))))

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
