; Copyright 2022 Vivid Inc. and/or its affiliates.

; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;    https://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

; Referencing https://plumatic.github.io/prismatics-graph-at-strange-loop
; Referencing figwheel-main/src/figwheel/main/watching.clj

(ns leiningen.peppy
  (:require
    [nextjournal.beholder :as beholder]
    [leiningen.core.main :as main-lein]
    [vivid.peppy.log :as log]
    [clojure.java.shell])
  (:import
    (java.nio.file Path)))

(defn gzip-processor []
  )

; Places a gzip'ped copy alongside the original. End of the pipeline.
(def ^:const gzip-process
  {:file-extensions #{"css" "html" "js" "svg"}
   :path            "src/main/content"
   :transformer-fn  gzip-processor})

(defn file-extension [s]
  (second (re-find #"\.([a-zA-Z0-9]+)$" s)))

(defn accept-file [^Path path accepted-file-extensions]
  (let [filename (.toString (.getFileName path))
        file-ext (file-extension filename)
        dir      (.getParent path)
        accept?  (get accepted-file-extensions file-ext)]
    (when accept?
      {:path     path
       :filename filename
       :file-ext file-ext
       :dir      dir})))

; TODO Load everything into a queue. Process when at max capacity or contents stabilizes after 100ms.

(defn gzip-process-one [input-path dest-path]
  (prn "PROCESS" input-path " -> " dest-path))
(defn gzip-delete-one [_ dest-path]
  (prn "DELETE" dest-path))

(defn gzip-watch [{:keys [type path] :as event}]
  (let [i (accept-file path (:file-extensions gzip-process))
        dest-path (.resolveSibling path (str (:filename i) ".gz"))]
    (if i
      (cond
        (get #{:create :modify} type)
        (gzip-process-one i dest-path)

        (= :delete type)
        (gzip-delete-one i dest-path)

        :else
        (log/*warn-fn* "Unknown beholder event:" (pr-str event))))
    (log/*debug-fn* "Ignoring event:" (pr-str event))))

; Leiningen entry point for lein-peppy
(defn peppy
  "Run an asset pipeline."
  [project & args]
  (binding [log/*debug-fn* main-lein/debug
            log/*info-fn* main-lein/info
            log/*warn-fn* main-lein/warn]
    (log/*info-fn* "Peppy getting straight to work")
    (beholder/watch gzip-watch "src/main/content")
    (while true
      (Thread/sleep Long/MAX_VALUE))))




; TODO Pass-through copying of files.
; TODO Observe the configuration file, hot reload it on changes, reject reload when there are config errors.
; TODO Re-run graph in response to Observer input.
