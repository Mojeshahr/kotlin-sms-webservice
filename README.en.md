<div align="center">

<a href="https://payam-resan.com">
  <img src=".github/assets/logo.svg" width="64" height="64" alt="Payam Resan">
</a>

<h1>Kotlin examples for the Payam Resan SMS web service</h1>

Talk to the <a href="https://payam-resan.com"><b>Payam Resan SMS panel</b></a> from Kotlin<br>
One runnable file per API method, for Android and for the server

[![API](https://img.shields.io/badge/API-V3-0a7cbd)](https://payam-resan.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.8%2B-7f52ff)](https://kotlinlang.org)
[![Dependency](https://img.shields.io/badge/dependency-one%20per%20set-2ea44f)](#why-two-sets-of-examples)
[![License](https://img.shields.io/badge/license-MIT-6e7781)](LICENSE)

<a href="README.md">فارسی</a> · <b>English</b>

</div>

<sub>Looking for another language? The same examples exist for the others at
[github.com/Mojeshahr](https://github.com/Mojeshahr).</sub>

---

## Quick start

```bash
git clone https://github.com/Mojeshahr/kotlin-sms-webservice.git
cd kotlin-sms-webservice

./lib/get-jars.sh

export PAYAM_RESAN_API_KEY='123456-XXXXXXXXXXXXXXX'
export PAYAM_RESAN_SENDER='30004040'

./run.sh examples/v3-jvm/account-info.kt
```

Start with `account-info.kt`. It sends nothing, spends no credit, and if it
answers then both the key and the connection are fine.

`run.sh` belongs to this repository alone. Kotlin has no single-file launcher
the way `java` does, so the script compiles the file and runs it, which keeps
the examples themselves free of build boilerplate. No example depends on it.

## Why two sets of examples

Kotlin is one language on two platforms, and a library that exists on one is
missing on the other:

| | `examples/v3` Android | `examples/v3-jvm` server |
|---|---|---|
| HTTP client | OkHttp | `java.net.http` from the JDK |
| JSON parser | `org.json`, part of Android | Gson |
| Dependency needed | one, OkHttp | one, Gson |

The split is forced by a fact, not a preference: **`java.net.http` does not
exist on Android.** It is a JDK 11 API that the Android SDK does not ship, and
it is absent from
[Google's desugaring list](https://developer.android.com/studio/write/java11-minimal-support-table)
too, so the server file does not even compile for Android. The reverse is also
true: `org.json` is free on Android and absent from the JDK.

Each set takes exactly one dependency, the same rule the Java repository
follows.

## Android and the account key

Do not put the key inside the app. Any string shipped in an APK can be
extracted, and whoever extracts it sends SMS on your credit. Not in code, not
in `BuildConfig`, not in `strings.xml`, not in an `.so`.

It belongs on your own backend. The app asks your server to "send a code to
this user", your server decides and calls this service with its own key. That
is why the Android functions take the key as a **parameter** instead of reading
an environment variable: an Android app has no environment variables.

The examples in that set call the service directly so you can see the shape of
the request. The shape is the same either way; in a real product, make the call
from your server.

## Android and the main thread

The functions block. Call one on the main thread and you get
`NetworkOnMainThreadException`. Turning it into a `suspend` function is two
lines:

```kotlin
suspend fun accountInfoAsync(apiKey: String) = withContext(Dispatchers.IO) {
    accountInfo(apiKey)
}
```

That needs `kotlinx-coroutines`, which every current Android project already
has. It is left out of the examples so each file works with one dependency.

## Before sending anything real

There is a sandbox server that answers exactly like production but sends no
message and spends no credit. Swap `V3` for `V3SandBox` in the URL. The one
exception is `TokenList`, which the sandbox does not implement.

## The methods

| Method | Android | Server | What it does |
|---|---|---|---|
| `AccountInfo` | [account-info.kt](examples/v3/account-info.kt) | [account-info.kt](examples/v3-jvm/account-info.kt) | Credit and active lines |
| `Send` | [send.kt](examples/v3/send.kt) | [send.kt](examples/v3-jvm/send.kt) | Simple send over `GET` |
| `SendBulk` | [send-bulk.kt](examples/v3/send-bulk.kt) | [send-bulk.kt](examples/v3-jvm/send-bulk.kt) | One text to many recipients, with tracking ids |
| `SendMultiple` | [send-multiple.kt](examples/v3/send-multiple.kt) | [send-multiple.kt](examples/v3-jvm/send-multiple.kt) | A separate text per recipient |
| `TokenList` | [token-list.kt](examples/v3/token-list.kt) | [token-list.kt](examples/v3-jvm/token-list.kt) | The account's templates |
| `SendTokenSingle` | [send-token-single.kt](examples/v3/send-token-single.kt) | [send-token-single.kt](examples/v3-jvm/send-token-single.kt) | Send a template to one number |
| `SendTokenSingle` | [send-token-single-get.kt](examples/v3/send-token-single-get.kt) | [send-token-single-get.kt](examples/v3-jvm/send-token-single-get.kt) | The same, over `GET` |
| `SendTokenMulti` | [send-token-multi.kt](examples/v3/send-token-multi.kt) | [send-token-multi.kt](examples/v3-jvm/send-token-multi.kt) | One template, many recipients |
| `StatusById` | [status-by-id.kt](examples/v3/status-by-id.kt) | [status-by-id.kt](examples/v3-jvm/status-by-id.kt) | Status by the service's id |
| `StatusByUserTraceId` | [status-by-user-trace-id.kt](examples/v3/status-by-user-trace-id.kt) | [status-by-user-trace-id.kt](examples/v3-jvm/status-by-user-trace-id.kt) | Status by your own id |
| `GetInbox` | [get-inbox.kt](examples/v3/get-inbox.kt) | [get-inbox.kt](examples/v3-jvm/get-inbox.kt) | Messages people sent to your lines |

## Using this in your own project

Every example is deliberately free of any dependency on this repository, so
copying the file into your project is enough. Instead of `lib/get-jars.sh`,
declare the dependency in Gradle:

```kotlin
// Android
implementation("com.squareup.okhttp3:okhttp:4.12.0")

// server
implementation("com.google.code.gson:gson:2.11.0")
```

If your project already has Retrofit, OkHttp is on the classpath and nothing is
added. If you use Ktor Client or kotlinx.serialization, leave the HTTP layer
behind and take only the request body and the `Success` check.

## Things that will save you time

**Do not read the HTTP status code.** The service answers `200` to everything,
including a wrong key, so `answer.isSuccessful` proves nothing. Decide on the
`Success` field. Every example here does.

**Recipient numbers carry no leading zero.** Use `9121112222`, or
`989121112222` with the country code. A number that does not start with `9` or
`989` returns error code `13`.

**Do not encode the text twice.** `addQueryParameter` in the Android example
and `URLEncoder` in the server one each do it once already. Encode beforehand
and the message arrives full of `%D8`.

**Send a unique `UserTraceId` per recipient.** After a timeout it is the only
way to learn whether the message was registered.

## Key safety

The key is a secret. It does not belong in a code repository, in browser
JavaScript, or in a mobile app bundle. On a server it belongs in an environment
variable; on mobile it has to come from your own backend.

If a key leaks, issue a new one from the panel. A deleted key never comes back.

## Layout

| Path | What it holds |
|---|---|
| `examples/v3/` | One self-contained example per service operation, for Android |
| `examples/v3-jvm/` | The same for server-side Kotlin |
| `lib/get-jars.sh` | Fetches the one dependency each set needs |
| `run.sh` | Compiles and runs a single file |
| `.env.example` | The environment variables the examples read |

The `v3` in the path is deliberate. A new service version means a new
`examples/v<n>/`, with the existing folder left alone.

## Documentation and support

The full guide is at [docs.payam-resan.com](https://docs.payam-resan.com). The
machine-readable OpenAPI description is in
[sms-webservice-spec](https://github.com/Mojeshahr/sms-webservice-spec).

## License

MIT. Full text in [`LICENSE`](LICENSE).
