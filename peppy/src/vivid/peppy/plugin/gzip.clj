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

(ns vivid.peppy.plugin.gzip
  (:require
    [clojure.java.io :as io]
    [clojure.java.shell]
    [hawk.core :as hawk]
    [vivid.peppy.log :as log])
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

(defn gzip-process-one [input _]
  (let [sh-res (clojure.java.shell/sh "/usr/bin/gzip" "--force" "--keep" "--verbose" (.toString (:path input)))]
    (when (not (empty? (:out sh-res)))
      (prn (:out sh-res)))
    (when (not (empty? (:err sh-res)))
      (prn (:err sh-res)))
    (merge sh-res
           {:step :gzip})))

(defn gzip-delete-one [_ dest-path]
  (prn "gzip: Deleting path" dest-path)
  (io/delete-file (.toString dest-path)))

(defn in-event [_ {:keys [kind file] :as hawk-event}]
  (let [i (accept-file (.toPath file) (:file-extensions gzip-process))
        dest-path (.resolveSibling (.toPath file) (str (:filename i) ".gz"))]
    (if i
      (cond
        (get #{:create :modify} kind)
        (gzip-process-one i dest-path)

        (= :delete kind)
        (gzip-delete-one i dest-path)

        :else
        (prn "gzip: Unknown hawk event:" (pr-str hawk-event))))
    (prn "gzip: Ignoring hawk event:" (pr-str hawk-event))))

(defn watch [config]
  (prn "gzip: Watching path" (:dir config))
  (hawk/watch! [{:paths [(:dir config)]
                 :handler in-event}]))
