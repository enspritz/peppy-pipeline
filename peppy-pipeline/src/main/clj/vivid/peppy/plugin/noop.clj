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

(ns vivid.peppy.plugin.noop
  (:require
   [vivid.peppy.log :as log]
   [vivid.peppy.plugins :as plugins]))

; The symbol that the defmulti matches on is identical to this namespace.
(defmethod plugins/peppy-plugin 'vivid.peppy.plugin.noop
  [node-descriptor]
  {:run (fn [args]
          (let [desc (update-in node-descriptor [:inputs] plugins/normalize-inputs)
                recognized-inputs (filter #(= (:input-type %) :string) (:inputs desc))
                is                (map :val recognized-inputs)
                o                 (map #(str % "-" (rand-int 10)) is)
                _ (log/*info-fn* (pr-str {:args args :desc desc :is is :o o}))]
            {:outputs o}))})

; TODO (get-in args [:a :outputs]) -> normalize each -> add them to the :inputs of this node. Do it in (normalize-inputs).
