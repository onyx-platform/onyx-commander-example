(ns onyx-commander-example.jobs.basic
  (:require [onyx.job :refer [add-task register-job]]
            [onyx.tasks.core-async :as core-async-task]
            [onyx-commander-example.tasks.math :as math]))

(defn basic-job
  [batch-settings]
  (let [base-job {:workflow [[:in :inc]
                             [:inc :out]]
                  :catalog []
                  :lifecycles []
                  :windows []
                  :triggers []
                  :flow-conditions []
                  :task-scheduler :onyx.task-scheduler/balanced}]
    (-> base-job
        (add-task (core-async-task/input :in batch-settings))
        (add-task (math/inc-key :inc [:n] batch-settings))
        (add-task (core-async-task/output :out batch-settings)))))

(defmethod register-job "basic-job"
  [job-name config]
  (let [batch-settings {:onyx/batch-size 1 :onyx/batch-timeout 1000}]
    (basic-job batch-settings)))
