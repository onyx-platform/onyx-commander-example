(ns onyx-commander-example.tasks.math
    (:require [schema.core :as s]))

(defn inc-in-segment
  "A specialized version of update-in that increments a key in a segment"
  [ks segment]
  (update-in segment ks inc))

(def IncKeyTask
  {::inc-key [s/Keyword]})

(s/defn inc-key
  ([task-name :- s/Keyword task-opts]
   {:task {:task-map (merge {:onyx/name task-name
                             :onyx/type :function
                             :onyx/fn ::inc-in-segment
                             :onyx/params [::inc-key]}
                            task-opts)}
    :schema {:task-map IncKeyTask}})
  ([task-name :- s/Keyword
    ks :- [s/Keyword]
    task-opts]
   (inc-key task-name (merge {::inc-key ks} task-opts))))
