#!/usr/bin/env bash
#
# Compile and run one example.
#
#   ./run.sh examples/v3-jvm/account-info.kt
#   ./run.sh examples/v3/account-info.kt
#
# Kotlin has no single-file launcher the way java does, so the file is compiled
# to a temporary directory and then run. This script exists so the examples
# themselves stay free of build boilerplate; nothing inside an example depends
# on it, and copying one file into your own project is still enough.
#
# The classpath carries every jar in lib/, so the same command works for both
# sets. Which jars an example actually needs is written at the top of the file.

set -euo pipefail

FILE="${1:-}"
if [ -z "$FILE" ] || [ ! -f "$FILE" ]; then
	echo "usage: ./run.sh examples/v3-jvm/account-info.kt" >&2
	exit 1
fi

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JARS="$(find "$ROOT/lib" -name '*.jar' -print0 | tr '\0' ':')"

if [ -z "$JARS" ]; then
	echo "run: no jars in lib/. Run ./lib/get-jars.sh first." >&2
	exit 1
fi

OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT

kotlinc -nowarn -cp "$JARS" "$FILE" -d "$OUT"

# The class name kotlinc generates is the file name in PascalCase with a Kt
# suffix: account-info.kt becomes Account_infoKt. Rather than reproduce that
# rule, ask the compiled output what it produced.
MAIN="$(cd "$OUT" && find . -name '*Kt.class' | head -1 | sed 's#^\./##; s#\.class$##; s#/#.#g')"

exec java -cp "$OUT:$JARS" "$MAIN"
