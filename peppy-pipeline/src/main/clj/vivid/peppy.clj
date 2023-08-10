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
   [farolero.core]
   [plumbing.core :refer [fnk]]
   [plumbing.graph]
   [vivid.peppy.log :as log]
   [vivid.peppy.plugins :as plugins]))

(defn resolve-peppy-plugin
  "Resolve the plugin by require'ing its namespace, giving its (defmethod) the opportunity to register itself."
  [node-descriptor]
  (-> node-descriptor :plugin symbol require))

; Plan:
;   Assume :once mode for now.
;   Translate the pipeline description into a plumbing.graph/graph
;   Run the graph to produce all output files.

; Quote a fully-qualified function that complies with the peppy plugin fn spec.

(def my-pipeline-decl
  {:a {:plugin 'vivid.peppy.plugin.noop
       :inputs ["some-file"]}
   :b {:plugin 'vivid.peppy.plugin.noop
       :inputs :a}})

(def my-pipeline
  {:a (fnk [:as args]  (let [node-descriptor (:a my-pipeline-decl)
                             _               (resolve-peppy-plugin node-descriptor)
                             plugin          (plugins/peppy-plugin node-descriptor)
                             run             (:run plugin)]
                         (run args)))
   :b (fnk [a :as args] (let [node-descriptor (:b my-pipeline-decl)
                              _               (resolve-peppy-plugin node-descriptor)
                              plugin          (plugins/peppy-plugin node-descriptor)
                              run             (:run plugin)]
                          (run args)))})

(defn run-pipeline-once []
  (let [g   (plumbing.graph/compile my-pipeline)
        res (g {})]
    (log/*info-fn* (into {} res))))

(defn -main []
  (binding [log/*debug-fn* println
            log/*info-fn* println
            log/*warn-fn* println]
    (log/*info-fn* "Peppy getting straight to work")
    (farolero.core/handler-case (run-pipeline-once)
                                (:vivid.peppy/error [_ details]
                                                    (log/*warn-fn* (pr-str details))))))
