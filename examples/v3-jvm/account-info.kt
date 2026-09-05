// AccountInfo - اعتبار باقی‌مانده و خطوط فعال حساب.
//
// سبک‌ترین متد سرویس و بهترین راه آزمودن کلید: چیزی ارسال نمی‌کند، اعتباری
// مصرف نمی‌کند، و حتی با اعتبار صفر هم جواب می‌دهد.
//
// این مجموعه برای کاتلین سمت سرور است، مثل Ktor و اسپرینگ‌بوت. کلاینت HTTP از
// خود JDK می‌آید و تنها وابستگی‌اش Gson است، چون کتابخانه استاندارد جاوا
// پارسر JSON ندارد.
//
// برای اندروید این فایل کار نمی‌کند: بسته java.net.http روی اندروید وجود
// ندارد. آنجا examples/v3 را بردارید.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... ./run.sh examples/v3-jvm/account-info.kt

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
        .uri(URI.create("https://api.sms-webservice.com/api/V3/AccountInfo"))
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

    val result = response.getAsJsonObject("Result")
    println("اعتبار: ${result["Credit"]}")

    for (line in result.getAsJsonArray("AvailableSenders")) {
        println("خط: $line")
    }
}
// docs:end
