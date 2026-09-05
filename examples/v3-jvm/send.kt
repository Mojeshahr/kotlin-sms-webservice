// Send - ساده‌ترین ارسال، یک متن به چند شماره با یک درخواست GET.
//
// برای آزمایش سریع خوب است. در محیط عملیاتی SendBulk را بردارید: کلید را از
// نشانی بیرون می‌برد و برای هر گیرنده شناسه پی‌گیری می‌پذیرد.
//
// این مجموعه برای کاتلین سمت سرور است. کلاینت HTTP از خود JDK می‌آید و تنها
// وابستگی‌اش Gson است. برای اندروید examples/v3 را بردارید؛ بسته java.net.http
// روی اندروید وجود ندارد.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... PAYAM_RESAN_SENDER=... ./run.sh examples/v3-jvm/send.kt

// docs:start
import com.google.gson.JsonParser
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import kotlin.system.exitProcess

fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)

fun main() {
    // تابع encode دقیقاً یک بار encode می‌کند. اگر متن را خودتان هم پیش از
    // این encode کنید، پیامک با نویسه‌های %D8 به گوشی می‌رسد.
    val query = "ApiKey=" + encode(System.getenv("PAYAM_RESAN_API_KEY")) +
        "&Sender=" + encode(System.getenv("PAYAM_RESAN_SENDER")) +
        "&Text=" + encode("کد تأیید شما ۱۲۳۴۵۶ است") +
        "&Recipients=" + encode("9121112222,9121113333")

    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.sms-webservice.com/api/V3/Send?$query"))
        .timeout(Duration.ofSeconds(30))
        .GET()
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
        println("شناسه ${item.asJsonObject["Id"]}")
    }
}
// docs:end
