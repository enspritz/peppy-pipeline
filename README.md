# Peppy Pipeline

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat-square)](LICENSE.txt)
[![Current version](https://img.shields.io/clojars/v/net.vivid-inc/peppy-pipeline.svg?color=blue&style=flat-square)](https://clojars.org/net.vivid-inc/peppy-pipeline)

A vivacious Clojure asset pipeline.

Tested with Clojure 1.10.0 and newer, and with Java 8 and newer LTS releases.

### Goals
- Automate static site build workflows by composing asset processing pipeline.
- For our (Vivid's) usecase, this is the last step to completely replacing Middleman as our SSG tool.

### Design
- You design processing pipeline by composing processing steps. Configure some file system watchers, specify the output dirs, then focus on your work and enjoyment. Gulp presents a simple design involving tasks, run in series() and parallel().
- Thinking of running the pipeline as a compute graph, fed by input events via file system watchers, etc:
  - https://github.com/thi-ng/fabric
  - https://github.com/plumatic/plumbing
  - https://news.ycombinator.com/item?id=4641465 https://gist.github.com/3874826
- Packaged with plugins require little configuration to Just Work and make you immediately productive.
- File system watchers, or conceptually anything else, feed inputs into the CG, triggering processing flow.
- The user configures the CG processing flow to push inputs through the processing pipeline, resulting in output.
- Users design Peppy output dir structure for production builds, figwheel live coding resources directory, whatever they have in mind.
- Born as a lein plugin, looking towards clj-tool and stand-alone.
- Determines nature of input by metadata provided by the plugin or by filename extension.
- Exploit the parallel nature of modern CPUs and high throughput of modern storage devices.
- Relies on the availability of task-specific, tried-and-trusted CLI tooling to accomplish the heavy lifting.
- Files-on-FS coupled with the DB (registry) approach to handling the flow of files between processing steps.

### Functions
- Queryable DB (registry) of all files. In particular, queryable from within ART templates.
- Frontmatter-like behavior, augmenting meta-data to the associated file in the DB.
- SASS compilation.
- ART templates.
- favicon generation.
- Optimize image files with svgo, pngcrush.
- Compress HTML (remove whitespaces).
- gzip certain file types in preparation for direct delivery by the HTTPd.
- Run modes: `auto` for live coding and `once` for production builds.
- Web GUI to inspect and monitor pipeline, processing steps, and performance.

### Similar work
- [mylesmegyesi/conveyor](https://github.com/mylesmegyesi/conveyor)
- [Gulp](https://gulpjs.com/)
- [Grunt](https://gruntjs.com/)
- [Rails Sprockets](https://github.com/rails/sprockets)
- [Stefon](https://github.com/circleci/stefon)
- [Optimus](https://github.com/magnars/optimus)
