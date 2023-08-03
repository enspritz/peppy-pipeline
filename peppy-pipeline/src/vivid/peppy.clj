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

; Referencing https://github.com/plumatic/plumbing/blob/master/test/plumbing/graph_examples_test.cljc

(ns vivid.peppy
  (:require
   [plumbing.core :refer [fnk sum]]
   [plumbing.graph]
   [vivid.peppy.log :as log]))



; Plan:
;   Assume :once mode for now.
;   Translate the pipeline description into a plumbing.graph/graph
;   Run the graph to produce output files.



(defmulti peppy-plugin (fn [plugin-descriptor] (:plugin plugin-descriptor)))

(defmethod peppy-plugin 'net.vivid-inc/noop
  [plugin-descriptor]
  {:run (fn []
          (let [i (:inputs plugin-descriptor)
                o (map #(str % "-" (rand-int 10)) i)]
           {:outputs o}))})



(def my-pipeline
  {:a (fnk []  (let [plugin-descriptor {:plugin 'net.vivid-inc/noop
                                        :inputs ["a"]}
                     plugin (peppy-plugin plugin-descriptor)
                     run (:run plugin)]
                 (run)))
   :b (fnk [a] (let [plugin-descriptor {:plugin 'net.vivid-inc/noop
                                        :inputs (:outputs a)}
                     plugin (peppy-plugin plugin-descriptor)
                     run (:run plugin)]
                 (run)))})

(defn run-sample-graph []
  (let [g   (plumbing.graph/compile my-pipeline)
        res (g {})]
    (log/*info-fn* (into {} res))))

(defn -main []
  (binding [log/*debug-fn* println
            log/*info-fn* println
            log/*warn-fn* println]
    (log/*info-fn* "Peppy getting straight to work")
    (run-sample-graph)))
