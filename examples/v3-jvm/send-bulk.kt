// SendBulk - یک متن به چند گیرنده، هر کدام با شناسه پی‌گیری خودتان.
//
// روش پیشنهادی برای ارسال عملیاتی. کلید در بدنه درخواست می‌رود نه در نشانی،
// و برای هر گیرنده UserTraceId می‌پذیرد تا گزارش تحویل را بدون نگه‌داشتن Id
// سامانه بگیرید.
//
// این مجموعه برای کاتلین سمت سرور است. کلاینت HTTP از خود JDK می‌آید و تنها
// وابستگی‌اش Gson است. برای اندروید examples/v3 را بردارید؛ بسته java.net.http
// روی اندروید وجود ندارد.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... PAYAM_RESAN_SENDER=... ./run.sh examples/v3-jvm/send-bulk.kt

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
    val recipients = JsonArray()
    recipients.add(JsonObject().apply {
        addProperty("Destination", 9121112222L)
        addProperty("UserTraceId", 1001L)
    })
    recipients.add(JsonObject().apply {
        addProperty("Destination", 9121113333L)
        addProperty("UserTraceId", 1002L)
    })

    val payload = JsonObject().apply {
        addProperty("ApiKey", System.getenv("PAYAM_RESAN_API_KEY"))
        addProperty("Sender", System.getenv("PAYAM_RESAN_SENDER").toLong())
        addProperty("Text", "سفارش شما ثبت شد.")
        add("Recipients", recipients)
    }

    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.sms-webservice.com/api/V3/SendBulk"))
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
        println("${message["UserTraceId"]} => شناسه ${message["Id"]}")
    }
}
// docs:end
