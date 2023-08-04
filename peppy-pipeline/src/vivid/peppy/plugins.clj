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

(ns vivid.peppy.plugins
  (:require
   [clojure.spec.alpha :as s]))

(defmulti peppy-plugin (fn [node-descriptor] (:plugin node-descriptor)))

;
; :inputs
;

(defn normalize-inputs
  ":inputs from the peppy pipeline declaration are normalized into a consistent form for consumption by plugins.
  An input element of
      :a
  is interpreted as receive the output of pipeline node :a, and becomes [{:input-type :pipe-from :node-id :a}].
  Likewise for
      [:a]

  Individual plugins decide how to treat each of the normalized inputs provided to them."
  [inputs-decl]
  (let [f (fn f [x]
            (cond (keyword? x) {:input-type :pipe-from :node-id x}
                  (string? x)  {:input-type :string :val x}
                  (coll? x)    (map f x)
                  :else        (farolero.core/signal :vivid.peppy/error {:message     "Unknown :input item"
                                                                         :item        x
                                                                         :inputs-decl inputs-decl})))]
    (->> (f inputs-decl)
         (flatten)
         (vec))))

(s/def :vivid.peppy/input-type #{:pipe-from :string})
(s/def :vivid.peppy/plugin-input
  (s/keys :req-un [:vivid.peppy/input-type]))
(s/fdef normalize-inputs
        :ret (s/coll-of :vivid.peppy/plugin-input))
