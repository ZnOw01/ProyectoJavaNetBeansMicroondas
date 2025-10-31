#!/usr/bin/env bash
set -euo pipefail

# Script para regenerar el analizador léxico (JFlex) y sint áctico (CUP)
# después de modificar los archivos .jflex o .cup

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${PROJECT_ROOT}"

echo "Regenerando analizador léxico con JFlex..."
if [[ ! -f "libs/JFlex.jar" ]]; then
    echo "ERROR: No se encontró libs/JFlex.jar"
    echo "Ejecuta primero: ./scripts/fetch-libs.sh"
    exit 1
fi

java -jar libs/JFlex.jar src/Cup/entradaCup.jflex
echo "✓ Lexer regenerado: src/Cup/LexerCup.java"

echo ""
echo "Regenerando analizador sintáctico con CUP..."
if [[ ! -f "libs/java-cup-11a.jar" ]]; then
    echo "ERROR: No se encontró libs/java-cup-11a.jar"
    echo "Ejecuta primero: ./scripts/fetch-libs.sh"
    exit 1
fi

# Generar el parser con CUP
java -jar libs/java-cup-11a.jar -parser Syntactic -destdir src/Cup src/Cup/Grammar.cup

echo "✓ Parser regenerado: src/Cup/Syntactic.java"
echo "✓ Símbolos regenerados: src/Cup/sym.java"
echo ""
echo "¡Analizadores regenerados exitosamente!"
