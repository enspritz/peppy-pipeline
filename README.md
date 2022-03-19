# Peppy Pipelines

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat-square)](LICENSE.txt)
[![Current version](https://img.shields.io/clojars/v/net.vivid-inc/peppy-pipeline.svg?color=blue&style=flat-square)](https://clojars.org/net.vivid-inc/peppy-pipeline)

A vivacious Clojure asset pipeline.

Tested with Clojure 1.10.0 and newer, and with Java 8 and newer LTS releases.

### Goals
- As in rails/sprockets, completely replace Middleman as our SSG tool.

### Design
- You interact with peppy mainly by designing a Computational Graph of processing pipelines. Configure some file system watchers, specify the output dirs, then focus on your work and enjoyment.
  - https://github.com/thi-ng/fabric
  - https://github.com/plumatic/plumbing
  - https://news.ycombinator.com/item?id=4641465 https://gist.github.com/3874826
- Packaged with plugins require little configuration to Just Work and make you immediately productive.
- File system watchers, or conceptually anything else, feed inputs into the CG, triggering processing flow.
- The user configures the CG processing flow to push inputs through the processing pipeline, resulting in output.
- The output dir is suitable for production builds, figwheel live coding resources directory contents.
- Lives as a Lein plugin and more.
- Determines nature of input by metadata provided by the input plugin or by filename extension, MIME type, file magic, etc. If i.e. an art template file `index.html.art` is being processed,
- Exploit the parallel nature of modern CPUs and high throughput of modern storage devices.

### Functions
- Queryable registry of all files. In particular, queryable from within ART templates.
- Frontmatter behavior.
- SASS compilation.
- ART templates.
- Optimize image files with svgo, pngcrush.
- gzip certain file types for direct delivery by the HTTP daemon to requesters.

### Similar work
- https://github.com/mylesmegyesi/conveyor
- https://github.com/rails/sprockets
- https://github.com/circleci/stefon
- https://github.com/edgecase/dieter
