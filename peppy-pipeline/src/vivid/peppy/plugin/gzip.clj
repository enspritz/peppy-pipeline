; Copyright 2023 Vivid Inc. and/or its affiliates.
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     https://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns vivid.peppy.plugin.gzip
  (:require
   [clojure.java.io :as io]
   [clojure.java.shell]
   [clojure.string]
   [hawk.core :as hawk])
  (:import
   (java.nio.file Path)))

; Places a gzip'ped copy alongside the original. End of the pipeline.
(def ^:const gzip-process
  {:file-extensions #{"css" "html" "js" "svg"}
   :path            "src/main/content"})

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

(defn gzip-file [in]
  (println "gzip: Compressing path" (.toString (:path in)))
  (let [sh-res (clojure.java.shell/sh "/usr/bin/gzip" "--force" "--keep" "--verbose" (.toString (:path in)))]
    (when (not= (:exit sh-res) 0)
      (println "gzip: gzip exited with non-zero status:" (pr-str sh-res)))
    #_(when (not (empty? (:out sh-res)))
        (log/debug (:out sh-res)))
    #_(when (not (empty? (:err sh-res)))
        (log/debug (:err sh-res)))
    (merge in
           {:peppy-action :add-dest-file
            :shell-result sh-res})))

(defn delete-file [in]
  (println "gzip: Deleting path" (.toString (:dest-path in)))
  (io/delete-file (.toString (:dest-path in)))
  (merge in
         {:peppy-action :delete-dest-file}))

(defn in-event [_ {:keys [kind file] :as hawk-event}]
  (if-let [in (accept-file (.toPath file) (:file-extensions gzip-process))]
    (let [in (merge in
                    {:dest-path (.resolveSibling (.toPath file) (str (:filename in) ".gz"))})]
      (cond
        (get #{:create :modify} kind)
        (gzip-file in)

        (= :delete kind)
        (delete-file in)

        :else
        (prn "gzip: Unknown hawk event:" (pr-str hawk-event))))
    #_(log/trace "gzip: Ignoring hawk event:" hawk-event)))

(defn watch [config]
  (let [paths (:paths config)]
    ; Ensure the path exists, otherwise hawk will throw an exception
    (doseq [p paths]
      (io/make-parents (clojure.java.io/file p "peppy")))
    ; Direct hawk to watch the specified directories
    (println "gzip: Watching paths" (clojure.string/join " " paths))
    (hawk/watch! [{:paths   paths
                   :handler in-event}])))

(defn list-files [^String path]
  (filter #(.isFile %) (file-seq (clojure.java.io/file path))))

(defn once [config]
  (let [files (mapcat list-files (:paths config))]
    (doseq [file files]
      (gzip-file {:path file}))))
