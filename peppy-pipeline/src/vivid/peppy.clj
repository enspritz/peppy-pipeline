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

; TODO Load everything into a queue. Process when at max capacity or contents stabilizes after 100ms debounce (configurable).
; TODO Observe the configuration file, hot reload it on changes, reject reload when there are config errors.
; TODO Re-run graph in response to input (watcher) events.
; TODO When first started, do all input files. Use file date times to see if output needs updating.
; TODO :continuous mode (depends on file metadata to determine if regen is necessary), :once mode (forces overwrite)

(ns vivid.peppy
  (:require
   [plumbing.core :refer [fnk sum]]
   [plumbing.graph]
   [vivid.peppy.log :as log]))

(def stats-graph-decl
  {:n  (fnk [xs]   (count xs))
   :m  (fnk [xs n] (/ (sum identity xs) n))
   :m2 (fnk [xs n] (/ (sum #(* % %) xs) n))
   :v  (fnk [m m2] (- m2 (* m m)))})

(defn run-sample-graph []
  (let [stats (plumbing.graph/compile stats-graph-decl)
        res   (stats {:xs [1 2 3 6]})]
    (log/*info-fn* (into {} res))))

(defn -main []
  (binding [log/*debug-fn* println
            log/*info-fn* println
            log/*warn-fn* println]
    (log/*info-fn* "Peppy getting straight to work")
    (run-sample-graph)))
