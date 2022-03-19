#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail
set -o xtrace

export TZ=UTC

# Aim for a clean build
find . -depth -name .cpcache -or -name out -or -name target | xargs rm -r || true

(cd peppy && lein install)
(cd lein-peppy && lein install)
