// TokenList - قالب‌های حساب، با کلید و متن و وضعیت تأییدشان.
//
// برای پیدا کردن TemplateKey که متدهای ارسال قالب لازم دارند. این متد هم مثل
// AccountInfo از بررسی اعتبار معاف است.
//
// این مجموعه برای کاتلین سمت سرور است. کلاینت HTTP از خود JDK می‌آید و تنها
// وابستگی‌اش Gson است. برای اندروید examples/v3 را بردارید؛ بسته java.net.http
// روی اندروید وجود ندارد.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... ./run.sh examples/v3-jvm/token-list.kt

// docs:start
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.system.exitProcess

fun main() {
    val payload = JsonObject()
    payload.addProperty("ApiKey", System.getenv("PAYAM_RESAN_API_KEY"))

    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.sms-webservice.com/api/V3/TokenList"))
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
        val template = item.asJsonObject
        val sendable = if (template["Status"].asInt == 2) "قابل ارسال" else "قابل ارسال نیست"
        println("${template["Key"].asString} ($sendable): ${template["TextTemplate"].asString}")
    }
}
// docs:end
