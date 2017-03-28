(ns onyx-commander-example.core
  (:require [clojure.java.io :as io]
            [clojure.tools.cli :refer [parse-opts]]
            [onyx-commander-example.spec.base-specs]
            [onyx-commander-example.spec.domain-specs]
            [onyx-commander-example.spec.command-specs]
            [onyx-commander-example.spec.event-specs]
            [lib-onyx.peer :as peer]
            [onyx.job]
            [onyx.api]
            [onyx.test-helper])
  (:gen-class))

(defn file-exists?
  "Check both the file system and the resources/ directory
  on the classpath for the existence of a file"
  [file]
  (let [f (clojure.string/trim file)
        classf (io/resource file)
        relf (when (.exists (io/as-file f)) (io/as-file f))]
    (or classf relf)))

(defn cli-options []
  [["-c" "--config FILE" "Aero/EDN config file"
    :default (io/resource "config.edn")
    :default-desc "resources/config.edn"
    :parse-fn file-exists?
    :validate [identity "File does not exist relative to the workdir or on the classpath"
               read-config "Not a valid Aero or EDN file"]]

   ["-p" "--profile PROFILE" "Aero profile"
    :parse-fn (fn [profile] (clojure.edn/read-string (clojure.string/trim profile)))]

   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Onyx Peer and Job Launcher"
        ""
        "Usage: [options] action [arg]"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  start-peers [npeers]    Start Onyx peers."
        "  submit-job  [job-name]  Submit a registered job to an Onyx cluster."
        ""]
       (clojure.string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (clojure.string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn assert-job-exists [job-name]
  (let [jobs (methods onyx.job/register-job)]
    (when-not (contains? jobs job-name)
      (exit 1 (error-msg (into [(str "There is no job registered under the name " job-name "\n")
                                "Available jobs: "] (keys jobs)))))))

(defn -main [& args]
  (let [{:keys [options arguments errors summary] :as pargs} (parse-opts args (cli-options))
        action (first args)
        argument (clojure.edn/read-string (second args))]
    (cond (:help options) (exit 0 (usage summary))
          (not= (count arguments) 2) (exit 1 (usage summary))
          errors (exit 1 (error-msg errors)))
    (case action
      "start-peers" (let [{:keys [env-config peer-config] :as config}
                          (read-config (:config options) {:profile (:profile options)})]
                      (peer/start-peer argument peer-config env-config))

      "submit-job" (let [{:keys [peer-config] :as config}
                         (read-config (:config options) {:profile (:profile options)})
                         job-name (if (keyword? argument) argument (str argument))]
                     (assert-job-exists job-name)
                     (let [job-id (:job-id
                                   (onyx.api/submit-job peer-config
                                                        (onyx.job/register-job job-name config)))]
                       (println "Successfully submitted job: " job-id)
                       (println "Blocking on job completion...")
                       (onyx.test-helper/feedback-exception! peer-config job-id)
                       (exit 0 "Job Completed"))))))
