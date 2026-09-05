// StatusById - وضعیت پیامک با شناسه‌هایی که متد ارسال برگردانده است.
//
// دسته‌ای بپرسید، نه یکی‌یکی. فاصله استعلام‌ها را هم کمتر از چند دقیقه
// نگذارید، وگرنه به خطای ۲۰ می‌خورید.
//
// این مجموعه برای کاتلین سمت سرور است. کلاینت HTTP از خود JDK می‌آید و تنها
// وابستگی‌اش Gson است. برای اندروید examples/v3 را بردارید؛ بسته java.net.http
// روی اندروید وجود ندارد.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... ./run.sh examples/v3-jvm/status-by-id.kt

// docs:start
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.system.exitProcess

// شرط را روی StatusCode بگذارید، نه روی متن Status. این پنج کد یعنی هنوز در
// راه است و باید بعداً دوباره استعلام کنید، نه اینکه دوباره بفرستید.
val PENDING = setOf(0, 1, 2, 3, 10)

fun main() {
    val ids = JsonArray()
    ids.add(9903211L)
    ids.add(9903212L)

    val payload = JsonObject().apply {
        addProperty("ApiKey", System.getenv("PAYAM_RESAN_API_KEY"))
        add("Ids", ids)
    }

    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.sms-webservice.com/api/V3/StatusById"))
        .header("Content-Type", "application/json; charset=utf-8")
        .timeout(Duration.ofSeconds(30))
        .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
        .build()

    val raw = HttpClient.newHttpClient()
        .send(request, HttpResponse.BodyHandlers.ofString())
        .body()

    val response = JsonParser.parseString(raw).asJsonObject

    if (response["Success"]?.asBoolean != true) {
        System.err.println("ناموفق. کد ${response["ErrorCode"]}: ${response["Error"]}")
        exitProcess(1)
    }

    for (item in response.getAsJsonArray("Result")) {
        val message = item.asJsonObject
        val again = if (message["StatusCode"].asInt in PENDING) " (بعداً دوباره بپرسید)" else ""
        println("${message["Id"]}: ${message["Status"].asString}$again")
    }
}
// docs:end
