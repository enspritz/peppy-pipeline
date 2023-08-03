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
   [vivid.peppy.log :as log]))

; Plan:
;   Assume :once mode for now.
;   Translate the pipeline description into a plumbing.graph/graph
;   Run the graph to produce all output files.

(def my-pipeline-decl
  {:a {:plugin 'net.vivid-inc/noop
       :inputs ["some-file"]}
   :b {:plugin 'net.vivid-inc/noop
       :inputs :a}})

; :inputs is transformed:
;
;   :a   becomes [{:type :plugin :id :a}]
;
;   [:a] becomes            "
;
;   (input-path "src") becomes [{:type :filepath :path "src/a.html"}
;                               {:type :filepath :path "src/b.txt"}]
;
;   (input-path "src" :extensions #{"html"}) becomes [{:type :filepath :path "src/a.html"}]

(defn normalize-inputs
  "Each plugin decides what it wants to do with each of the inputs provided to it."
  [inputs-decl]
  (let [f (fn f [x]
            (cond (keyword? x) {:type :plugin :id x}
                  (string? x)  {:type :string :val x}
                  (coll? x)    (map f x)
                  :else        (farolero.core/signal :vivid.peppy/error {:message     "Unknown :input item"
                                                                         :item        x
                                                                         :inputs-decl inputs-decl})))]
    (->> (f inputs-decl)
         (flatten)
         (vec))))

(defmulti peppy-plugin (fn [plugin-descriptor] (:plugin plugin-descriptor)))

(defmethod peppy-plugin 'net.vivid-inc/noop
  [plugin-descriptor]
  {:run (fn []
          (let [desc (update-in plugin-descriptor [:inputs] normalize-inputs)
                is   (as-> (:inputs desc) $
                       (filter #(= (:type %) :string) $)
                       (map :val $))
                o    (map #(str % "-" (rand-int 10)) is)
                _ (log/*info-fn* (pr-str {:desc desc :is is :o o}))]
            {:outputs o}))})

(def my-pipeline
  {:a (fnk []  (let [plugin-descriptor {:plugin 'net.vivid-inc/noop
                                        :inputs 123}
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
    (farolero.core/handler-case (run-sample-graph)
                                (:vivid.peppy/error [_ details]
                                 (log/*warn-fn* (pr-str details))))))
