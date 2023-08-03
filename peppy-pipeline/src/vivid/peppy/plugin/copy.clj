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

(ns vivid.peppy.plugin.copy
  (:require
    [clojure.java.shell]))

#_{:type        :copy
   :src-paths   ["src/main/content"]
   :dest-path   "target/build"}

(defn once [{:keys [src-paths dest-path] :as config}]
  ; TODO Copy files one by one. Indicate whether file is updated or skipped.
  (let [cmd (concat ["/usr/bin/cp" "--recursive" "--update" "--verbose"]
                    src-paths
                    [dest-path])
        result (apply clojure.java.shell/sh cmd)]
    (when (not= (:exit result) 0)
      (println "copy: cp exited with non-zero status:" (pr-str result)))
    (when (not (empty? (:out result)))
        (println "copy:" (:out result)))
    (when (not (empty? (:err result)))
        (println "copy:" (:err result)))))
