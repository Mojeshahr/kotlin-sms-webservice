// SendMultiple - متن و خط فرستنده جدا برای هر گیرنده.
//
// برای پیام‌های شخصی‌سازی‌شده که با یک قالب ثابت پوشش داده نمی‌شوند. برخلاف
// SendBulk، اینجا Text و Sender در سطح هر گیرنده تعریف می‌شوند.
//
// این مجموعه برای کاتلین سمت سرور است. کلاینت HTTP از خود JDK می‌آید و تنها
// وابستگی‌اش Gson است. برای اندروید examples/v3 را بردارید؛ بسته java.net.http
// روی اندروید وجود ندارد.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... PAYAM_RESAN_SENDER=... ./run.sh examples/v3-jvm/send-multiple.kt

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
    val sender = System.getenv("PAYAM_RESAN_SENDER").toLong()

    val recipients = JsonArray()
    recipients.add(JsonObject().apply {
        addProperty("Sender", sender)
        addProperty("Destination", 9121112222L)
        addProperty("Text", "آقای محمدی، سفارش شما ارسال شد.")
        addProperty("UserTraceId", 1001L)
    })
    recipients.add(JsonObject().apply {
        addProperty("Sender", sender)
        addProperty("Destination", 9121113333L)
        addProperty("Text", "خانم رضایی، سفارش شما ارسال شد.")
        addProperty("UserTraceId", 1002L)
    })

    val payload = JsonObject().apply {
        addProperty("ApiKey", System.getenv("PAYAM_RESAN_API_KEY"))
        add("Recipients", recipients)
    }

    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.sms-webservice.com/api/V3/SendMultiple"))
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
