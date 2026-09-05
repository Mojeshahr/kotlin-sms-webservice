// StatusByUserTraceId - وضعیت پیامک با شناسه‌هایی که خودتان داده‌اید.
//
// اگر UserTraceId را کلید رکورد پایگاه داده خودتان بگذارید، دیگر لازم نیست Id
// سامانه را ذخیره کنید. این متد راه امن تشخیص ارسال تکراری هم هست: بعد از قطع
// ارتباط، اول اینجا بپرسید ثبت شده یا نه.
//
// این مجموعه برای کاتلین سمت سرور است. کلاینت HTTP از خود JDK می‌آید و تنها
// وابستگی‌اش Gson است. برای اندروید examples/v3 را بردارید؛ بسته java.net.http
// روی اندروید وجود ندارد.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... ./run.sh examples/v3-jvm/status-by-user-trace-id.kt

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

fun main() {
    val traceIds = JsonArray()
    traceIds.add(1001L)
    traceIds.add(1002L)

    val payload = JsonObject().apply {
        addProperty("ApiKey", System.getenv("PAYAM_RESAN_API_KEY"))
        add("UserTraceIds", traceIds)
    }

    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.sms-webservice.com/api/V3/StatusByUserTraceId"))
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

        // کد ۸ یعنی این شناسه در حساب شما نیست. بعد از یک timeout، همین یعنی
        // ارسال ثبت نشده و می‌توانید با خیال راحت دوباره بفرستید.
        if (message["StatusCode"].asInt == 8) {
            println("${message["UserTraceId"]}: ثبت نشده")
            continue
        }

        println("${message["UserTraceId"]}: ${message["Status"].asString}")
    }
}
// docs:end
