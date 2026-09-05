# Agent guide

Runnable Kotlin examples for the Payam Resan SMS web service. One file per API
method, twice: once for Android and once for server-side Kotlin.

## Rule one: two sets, and they are not interchangeable

Kotlin is one language on two platforms with different libraries. The split is
forced, not stylistic:

| | `examples/v3` Android | `examples/v3-jvm` server |
|---|---|---|
| HTTP | OkHttp | `java.net.http` |
| JSON | `org.json` | Gson |
| dependency | one, OkHttp | one, Gson |

`java.net.http` is a JDK 11 API that Android does not ship and that Google's
desugaring list does not cover, so a server file will not compile for Android.
`org.json` is part of Android and absent from the JDK, so the reverse fails
too. Do not try to merge the two sets, and do not add a second dependency to
either: one library per set is the line, the same as the Java repository.

`kotlinx-coroutines` is deliberately not a dependency. The functions block and
the README shows the two-line `withContext(Dispatchers.IO)` wrapper.

## Rule two: on Android the key is a parameter

An Android app has no environment variables, and a key shipped inside an APK
can be extracted. So the Android functions take `apiKey: String` and the header
of every file says the key must come from the reader's own backend.

The server set reads `PAYAM_RESAN_API_KEY` from the environment, like every
other repository in this organisation.

Whichever set you touch, the key never appears in the code.

## Rule three: the examples are the documentation

Each file carries `// docs:start` and `// docs:end`. The region between them is
lifted verbatim into the method's page on docs.payam-resan.com, so it is read by
people who have never seen this repository.

Two consequences:

- **Full-line comments are stripped** when the region is lifted. Anything the
  reader must see has to be code. The `Success` check is a `check` or an `if`,
  not a note.
- The file name matches the reference page slug exactly: `send-bulk.kt`,
  `status-by-user-trace-id.kt`. A path with two variants gets two files, the
  plain name for `POST` and a `-get` suffix for `GET`.

In the Android set, `fun main` sits **below** `docs:end` on purpose. It makes
the file runnable on a desktop for checking, while the documentation tab shows
only the function a reader would actually call.

The full contract lives in the `handbook` repository, section `docs-site`, file
`code-samples.md`.

## Rule four: check Success, never the status code

The service answers `200` to everything, including a wrong key and an empty
account, so `answer.isSuccessful` proves nothing:

```kotlin
check(response.optBoolean("Success")) {
    "ناموفق. کد ${response.opt("ErrorCode")}: ${response.optString("Error")}"
}
```

The server set uses `exitProcess(1)` after printing to stderr instead, matching
the Java repository. Either way the process must end non-zero.

Read the error out of the JSON element rather than calling `asString` on it. A
body that is not the usual envelope then prints something readable instead of
throwing on the error path, which is the worst possible place to throw.

## Rule five: a version is a folder

A new service version means a new `examples/v<n>/` and `examples/v<n>-jvm/`. No
file inside an existing version folder is moved or renamed; older versions still
have users.

## Secrets

No key, no real phone number and no customer name goes into a file here, not
even a dead one. Example numbers are `9121112222` upward and the example key is
`123456-XXXXXXXXXXXXXXX`.

## Layout

| Path | What it holds |
|---|---|
| `examples/v3/` | one self-contained file per service operation, Android |
| `examples/v3-jvm/` | the same for server-side Kotlin |
| `lib/get-jars.sh` | fetches the one dependency each set needs |
| `run.sh` | compiles and runs a single file |
| `.env.example` | the environment variables the server examples read |

## Before every commit

Compile every file **on its own**. They cannot be compiled together: each
declares its own top-level `main`, and several declare their own `JSON` or
`PENDING`, which collide in one compilation unit.

```bash
./lib/get-jars.sh
for f in examples/v3/*.kt examples/v3-jvm/*.kt; do ./run.sh "$f" >/dev/null || echo "FAILED $f"; done
```

Point them at `api/V3SandBox/` before running one for real, so no message goes
out.

## Git

Semantic messages, `type(scope): subject`, with no explanatory body and no
attribution trailer. Commits here are authored as Payam Resan.
