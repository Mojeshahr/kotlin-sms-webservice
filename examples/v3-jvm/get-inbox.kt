// GetInbox - پیامک‌هایی که کاربران به خطوط حساب شما فرستاده‌اند.
//
// این یک استعلام است، نه webhook: سامانه چیزی به سرور شما نمی‌فرستد و باید
// خودتان دوره‌ای صدایش بزنید. فاصله را کمتر از چند دقیقه نگذارید، وگرنه به
// خطای ۲۰ می‌خورید.
//
// این مجموعه برای کاتلین سمت سرور است. کلاینت HTTP از خود JDK می‌آید و تنها
// وابستگی‌اش Gson است. برای اندروید examples/v3 را بردارید؛ بسته java.net.http
// روی اندروید وجود ندارد.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... ./run.sh examples/v3-jvm/get-inbox.kt

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
        .uri(URI.create("https://api.sms-webservice.com/api/V3/GetInbox"))
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
        val sms = item.asJsonObject
        // نام فیلد فرستنده در خود سرویس Form است، نه From. دنبال From نگردید.
        println("${sms["Time"].asString}  ${sms["Form"]} -> ${sms["To"]}: ${sms["Text"].asString}")
    }
}
// docs:end
