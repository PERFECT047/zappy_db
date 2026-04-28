#!/bin/bash

set -euo pipefail

echo "Compiling ZappyDB benchmark sources..."
mvn -q -DskipTests test-compile

echo "Running ZappyDB benchmark..."
java -cp target/classes:target/test-classes org.perfect047.benchmark.ZappyDbBenchmark "$@"
