#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$BASE_DIR"
rm -rf out
mkdir -p out
javac -d out -cp lib/sqlite-jdbc.jar $(find . -name '*.java')
java -cp out:lib/sqlite-jdbc.jar FullTestRunner
