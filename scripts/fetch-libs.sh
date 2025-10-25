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
  curl -fL "$url" -o "$output"
}

echo "Fetching parser dependencies into ${LIBS_DIR}"

download "https://repo1.maven.org/maven2/de/jflex/jflex/1.6.1/jflex-1.6.1.jar" "${LIBS_DIR}/JFlex.jar"
download "https://repo1.maven.org/maven2/com/github/vbmacher/java-cup/11b-20160615/java-cup-11b-20160615.jar" "${LIBS_DIR}/java-cup.jar"
download "https://repo1.maven.org/maven2/com/github/vbmacher/java-cup-runtime/11b-20160615/java-cup-runtime-11b-20160615.jar" "${LIBS_DIR}/java-cup-runtime.jar"

echo "Done."
