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

(ns vivid.peppy.plugin.copy
  (:require
    [clojure.java.io :as io]
    [hawk.core :as hawk]))

(defn copy [in])
(defn delete [in])

(defn in-event [_ {:keys [kind file] :as hawk-event}]
  (cond
    (get #{:create :modify} kind)
    (copy nil)

    (= :delete kind)
    (delete nil)

    :else
    (prn "gzip: Unknown hawk event:" (pr-str hawk-event))))

(defn watch [config]
  (let [paths (:paths config)]
    ; Ensure the path exists, otherwise hawk will throw an exception
    (doseq [p paths]
      (io/make-parents (clojure.java.io/file p "peppy")))
    ; Direct hawk to watch the specified directories
    (println "copy: Watching paths" (clojure.string/join " " paths))
    (hawk/watch! [{:paths   paths
                   :handler in-event}])))
