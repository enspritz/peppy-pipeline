; Copyright 2022 Vivid Inc.

(defproject net.vivid-inc/peppy-pipelines "0.1.0-SNAPSHOT"

  :description "Peppy Pipelines, a vivacious pipeline and build tool."
  :url "https://github.com/vivid-inc/ash-ra-template"
  :license {:distribution :repo
            :name         "Apache License 2.0"
            :url          "https://www.apache.org/licenses/LICENSE-2.0"}

  :aliases {"build"     ["do"
                         ["version"]
                         ["clean"]
                         ["eftest"]
                         ;["cloverage"]
                         ;["clj-kondo"]
                         ["jar"]
                         ["install"]]}

  :eval-in-leiningen true

  :exclusions [org.clojure/clojure]

  :global-vars {*warn-on-reflection* true}

  :javac-options ["-target" "1.8"]

  :manifest {"Built-By" "vivid"}

  ; This version of Leiningen is what we have available to us in CI.
  :min-lein-version "2.9.1"

  ; Enable this to assist with determining :excludes whenever dependencies and
  ; plugins change, then re-disable it.
  ;:pedantic? :abort

  :plugins [[com.github.liquidz/antq "RELEASE" :exclusions [commons-codec
                                                            org.apache.httpcomponents/httpclient
                                                            org.codehaus.plexus/plexus-utils
                                                            org.slf4j/slf4j-api]]
            [lein-cljfmt "0.8.0" ]
            [lein-cloverage "1.2.2"]
            [lein-eftest "0.5.9"]
            [lein-nvd "1.4.1" :exclusions [com.fasterxml.jackson.core/jackson-annotations
                                           commons-io
                                           org.apache.commons/commons-lang3
                                           org.codehaus.plexus/plexus-utils
                                           org.slf4j/jcl-over-slf4j
                                           org.slf4j/slf4j-api]]]

  :repositories [["clojars" {:sign-releases false}]])
