# Topics in the development of Peppy Pipeline

## Open questions
- How are pipelines defined?
  There are strict rules: Acyclic. Plugin pre-conditions must be met for the entire pipeline to function.
```
(def my-pipeline {

  ; A pipeline node that accepts input, processes that input to produce output, and then makes that output available to other nodes as input, writing files out, etc.
  :scss      {; `scss-peppy`, a tool that compiles SCSS files to plain CSS files, is declared as the processor here at this node in the computation graph.
              ; It requires `scss` available on $PATH, and will fail the pipeline if this requirement is unsatisfied.
              :plugin 'net.vivid-inc/scss-peppy
              ; The plugin's processing input is whatever is defined in :inputs, in this case:
              ; `(input-path)` scans / watches the file system sub-tree at the designated path for certain files, according to its parameters.
              ; The plugin accepts any file, but will process only those files that it recognizes.
              :inputs (input-path "src/main/scss")}

  :art-svg   {; This node renders ART templates to output files.
              :plugin 'net.vivid-inc/art-peppy
              ; Looks like we are rendering SVG files from their ART templates.
              :inputs (input-paths "src/main/templates/svg")
              ; This configuration block is passed verbatim to the plugin, and anything goes.
              ; In this case, inputs and output directory are not listed; instead those are accepted by `art-peppy` and translated as necessary for whatever processing system it interfaces to.
              :config {:dependencies '[[hiccup/hiccup "1.0.5"]]}}

  :img-optim {:plugin 'net.vivid-inc/img-optim-peppy
              :inputs [:art-svg    ; All output files from the :art-svg node.
                       (input-path "src/main/graphics" :extensions #{"jpeg" "jpg" "png"})
                       (input-path "assets/gfx")]
              :config { ... img_optim config ... }}

  :gzip      {:plugin 'net.vivid-inc/gzip-peppy
              :inputs [:scss :img-optim]} })
```

## Ideas
- Use of plumbing/graph
  - Graphs are eager, parallel, as in `(graph/par-compile my-graph)`.
  - Log performance metrics on activity, as in `(graph/profiled ::profile-data my-graph)`
  - We use the graph for topological ordering of nodes and their inputs as well as running the pipeline; the output of running the graph function in Clojure is irrelevant to peppy.
- Load everything into a queue. Process when at max capacity or contents stabilizes after 100ms debounce (configurable).
- Observe the configuration file, hot reload it on changes, reject reload when there are config errors.
- Re-run graph in response to input (watcher) events.
  - At those nodes that watch for files, the "there was a filesystem event within this subtree" event is a signal that causes the graph to be re-run, starting at its associated graph node.
  - That node and everything downstream from it needs to be re-run.
- When first started, do all input files. Use file date times to see if output needs updating.
- CLI command is simple:
  - `:continuous` mode: The default. Depends on file metadata to determine if re-running its segment is necessary, specifically last modified / creation date, like `make` and friends.
  - `:once` mode: Forces overwrite of all intermediate and output files.
