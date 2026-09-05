#!/usr/bin/env bash
#
# Fetch the dependencies the examples need.
#
#   ./lib/get-jars.sh
#
# Kotlin has no JSON parser and no HTTP client of its own; both come from the
# platform it runs on, and the two platforms differ. That is why this repository
# carries two sets of examples and why each set needs exactly one library:
#
#   examples/v3      Android. OkHttp, because java.net.http does not exist there
#                    and Android's own org.json covers the JSON side for free.
#   examples/v3-jvm  Server. Gson, because the JDK has an HTTP client but no
#                    JSON parser.
#
# The org.json jar below is only for running the Android examples on a desktop
# JVM to try them out. An Android app does not need it: org.json is part of the
# platform.
#
# In a real project you would declare these in Gradle instead:
#
#   implementation("com.squareup.okhttp3:okhttp:4.12.0")     // Android
#   implementation("com.google.code.gson:gson:2.11.0")       // server

set -euo pipefail

OKHTTP="${OKHTTP_VERSION:-4.12.0}"
OKIO="${OKIO_VERSION:-3.6.0}"
GSON="${GSON_VERSION:-2.11.0}"
JSON="${JSON_VERSION:-20240303}"

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

fetch() {
	local name="$1" url="$2" target="$DIR/$1"
	if [ -f "$target" ]; then
		echo "get-jars: $name is already here, delete it to fetch again"
		return 0
	fi
	echo "get-jars: fetching $name"
	curl -fsSL "$url" -o "$target"
}

fetch okhttp.jar "https://repo1.maven.org/maven2/com/squareup/okhttp3/okhttp/$OKHTTP/okhttp-$OKHTTP.jar"
fetch okio.jar   "https://repo1.maven.org/maven2/com/squareup/okio/okio-jvm/$OKIO/okio-jvm-$OKIO.jar"
fetch gson.jar   "https://repo1.maven.org/maven2/com/google/code/gson/gson/$GSON/gson-$GSON.jar"
fetch json.jar   "https://repo1.maven.org/maven2/org/json/json/$JSON/json-$JSON.jar"

echo "get-jars: done. Run an example with  ./run.sh examples/v3-jvm/account-info.kt"
