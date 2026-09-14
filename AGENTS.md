# Working with this repository

You are looking at runnable Kotlin examples for the **Payam Resan** SMS web
service (`api.sms-webservice.com`, API V3), an Iranian SMS provider. Someone is
probably asking you to add SMS to an Android app or a Kotlin server.

The first decision is which of the two example sets applies. Get that wrong and
the code will not even compile.

## Rule 1: two sets, and they are not interchangeable

| | `examples/v3/` | `examples/v3-jvm/` |
|---|---|---|
| For | Android | server — Ktor, Spring Boot, plain JVM |
| HTTP | OkHttp | `java.net.http` |
| JSON | `org.json` | Gson |
| Key | a function parameter | `System.getenv(...)` |

The split is forced by a fact, not a preference. **`java.net.http` does not
exist on Android** — it is a JDK 11 API the Android SDK does not ship, and it is
not on Google's desugaring list either, so a server file will not compile for an
app. The reverse is also true: `org.json` is part of Android and absent from the
JDK.

Do not try to merge them, and do not "modernise" an Android example by swapping
in `java.net.http`.

Each set takes exactly one dependency — OkHttp on Android, Gson on the server.
If the project already has Retrofit, OkHttp is already on the classpath. If it
uses Ktor Client or `kotlinx.serialization`, keep the payload and the `Success`
check and replace only the transport.

## Rule 2: on Android, the key must not ship in the app

Any string in an APK can be extracted, and whoever extracts it sends SMS on the
user's credit. Not in code, not in `BuildConfig`, not in `strings.xml`, not in
an `.so`. Obfuscation does not help; the string still exists at runtime.

**The call belongs on the user's own backend.** The app asks their server "send
a code to this user", the server decides and calls this service with its own
key. That is why the Android functions take the key as a parameter:

```kotlin
fun sendBulk(apiKey: String, sender: Long)
```

An Android app has no environment variables. The `fun main` below `docs:end` in
those files exists only so the example can be run from a desktop to try it; in a
real app that part does not exist and the key comes from the backend.

For a one-time password this matters more, not less: the app must not get to
choose which code goes to which number.

## Rule 3: the functions block

Call one on the main thread and you get `NetworkOnMainThreadException`. Making
it a suspending function is two lines:

```kotlin
suspend fun accountInfoAsync(apiKey: String) = withContext(Dispatchers.IO) {
    accountInfo(apiKey)
}
```

That needs `kotlinx-coroutines`, which every current Android project already
has. It is left out of the examples so each file works with one dependency.

## Rule 4: `Success`, never the HTTP status

The service answers `200` to everything, including a wrong key, an empty account
and a malformed body.

Android, with opt-accessors so the error path cannot itself throw:

```kotlin
check(response.optBoolean("Success")) {
    "ناموفق. کد ${response.opt("ErrorCode")}: ${response.optString("Error")}"
}
```

Server, safe-called and printing the raw elements:

```kotlin
if (response["Success"]?.asBoolean != true) {
    System.err.println("ناموفق. کد ${response["ErrorCode"]}: ${response["Error"]}")
    exitProcess(1)
}
```

Read the error out of the JSON element rather than calling `asString` on it. A
body that is not the usual envelope then prints something readable instead of
throwing on the error path, which is the worst possible place to throw.

Field reads on the success path are strict on purpose — `getString`, `asInt` —
so a malformed success body fails loudly rather than silently.

## Rule 5: ids and phone numbers are `Long`

`9121112222` overflows `Int`, and so does a message id:

```kotlin
addProperty("Destination", 9121112222L)
```

On the way back, read ids as raw elements — `message["Id"]`, not `.asInt`, which
truncates. `asInt` is only safe for `StatusCode` and `Status`.

## Rule 6: pick the right method

| The user wants | Use | File |
|---|---|---|
| one text to many people | `SendBulk` | `send-bulk.kt` |
| a different text per person | `SendMultiple` | `send-multiple.kt` |
| a one-time password or code | `SendTokenSingle` | `send-token-single.kt` |
| a template to many people | `SendTokenMulti` | `send-token-multi.kt` |
| delivery status | `StatusByUserTraceId` | `status-by-user-trace-id.kt` |
| balance and sender lines | `AccountInfo` | `account-info.kt` |

**A one-time password goes through a template**, not free text — that is the
usual route for OTP, and the template fixes the sender line, which is why
`SendTokenSingle` takes no `Sender`. `token-list.kt` lists the account's
templates; `Status == 2` means approved and sendable, `1` awaiting review, `3`
rejected.

**Use the POST variant for OTP**, never `send-token-single-get.kt`. In the GET
form both the key and the code land in the URL and the web server log.

## Rule 7: encode the query exactly once

The server set has `URLEncoder.encode(value, StandardCharsets.UTF_8)`; the
Android set uses OkHttp's `addQueryParameter`. Both encode exactly once.
Pre-encode as well and the message arrives full of `%D8` sequences.

POST bodies carry `application/json; charset=utf-8` in both sets.

## Rule 8: phone numbers have no leading zero

The service wants `9121112222` or `989121112222`. Users type `09121112222` or
`+989121112222`. Normalise before sending, or you get error `13`.

Ninety-nine recipients per request is the ceiling for `SendBulk`,
`SendMultiple` and `SendTokenMulti`.

## Rule 9: always send a `UserTraceId`

Use the user's own database id. After a timeout or error `100`, resending blind
may send twice — `StatusByUserTraceId` is the only safe way to learn whether the
message was registered. `StatusCode == 8` there means the id is not in the
account, so it is safe to send again.

`SendTokenSingle` is the exception: it has no such input, so its `UserTraceId`
comes back null. If a trace id is needed for an OTP, use `SendTokenMulti` with a
single recipient.

## Rule 10: know which errors are worth retrying

These never succeed on retry — fix the cause; retrying only burns the rate limit
until the account hits error `20`:

`1`, `2`, `3`, `6`, `8`, `9`, `10`, `11`, `12`, `13`, `14`, `19`

`19` is an empty balance; `10` means the caller's IP is not on the account's
allowlist. Treat any unknown code the way you treat `100`: unclear outcome,
check with `StatusByUserTraceId` before resending.

## Rule 11: delivery status is a poll, not a callback

Status codes `0`, `1`, `2`, `3` and `10` mean still in flight — query again
later, and not more often than every few minutes or you will hit error `20`.
Everything else is final. Branch on `StatusCode`, never on the `Status` text,
which is Persian prose meant for humans and can change.

## Rule 12: `GetInbox` consumes what it returns

The service hands over each incoming message **once**. Never call it from an
Activity or a request handler: every call consumes unread messages permanently.
It belongs in a scheduled job on the server.

The sender field is called `Form`, not `From`. That is the service's spelling.

## Running an example from this repo

```bash
./lib/get-jars.sh
export PAYAM_RESAN_API_KEY='123456-XXXXXXXXXXXXXXX'
export PAYAM_RESAN_SENDER='30004040'
./run.sh examples/v3-jvm/account-info.kt
```

`get-jars.sh` fetches OkHttp, Okio, Gson and org.json into `lib/`; `run.sh`
compiles one file and runs it. Compile **one file at a time** — each declares
its own top-level `main`, and several declare their own `JSON` or `PENDING`,
which collide in a single compilation unit.

The org.json jar is only there so the Android examples can be tried on a desktop
JVM. A real Android app does not need it.

## Testing without spending credit

Replace `V3` with `V3SandBox` in the URL. No message is sent and no credit is
spent. `TokenList` is not implemented there.

The sandbox is a simulator, not a mirror of the account: credit is always
`1234567`, sender lines are invented, and **it accepts any key**. Success there
proves nothing about the user's real key.

## Where the authoritative answers are

- Method reference and error tables: <https://docs.payam-resan.com>
- Machine-readable OpenAPI: <https://github.com/Mojeshahr/sms-webservice-spec>

If the spec and these examples ever disagree, the spec wins — report it as a bug
rather than guessing.
