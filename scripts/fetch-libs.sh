#!/usr/bin/env bash
set -euo pipefail

LIBS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../libs" && pwd)"
mkdir -p "${LIBS_DIR}"

function download() {
  local url="$1"
  local output="$2"
  if [[ -f "${output}" ]]; then
    echo "- ${output##*/} already present"
    return
  fi
  echo "- downloading ${output##*/}"
  curl -L "$url" -o "$output"
}

echo "Fetching parser dependencies into ${LIBS_DIR}"

download "https://repo1.maven.org/maven2/de/jflex/jflex/1.6.1/jflex-1.6.1.jar" "${LIBS_DIR}/JFlex.jar"
download "https://repo1.maven.org/maven2/java-cup/java-cup/11a/java-cup-11a.jar" "${LIBS_DIR}/java-cup-11a.jar"
download "https://repo1.maven.org/maven2/java-cup/java-cup-runtime/11a/java-cup-runtime-11a.jar" "${LIBS_DIR}/java_cup.jar"

echo "Done."
