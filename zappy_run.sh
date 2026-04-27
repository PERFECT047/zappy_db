#!/bin/sh
#
# Use this script to run your program LOCALLY.

set -e # Exit early if any commands fail

(
  cd "$(dirname "$0")" # Ensure compile steps are run within the repository directory
  mvn compile
)

exec java --enable-preview -cp target/classes org.perfect047.Main "$@"
