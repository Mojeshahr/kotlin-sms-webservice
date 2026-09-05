// SendTokenSingle با GET - همان ارسال قالب، با ورودی در نشانی.
//
// برای آزمایش دستی مناسب است، برای محیط عملیاتی نه: در GET هم کلید حساب و هم
// مقدار رمز یک‌بارمصرف داخل نشانی می‌نشینند و در لاگ وب‌سرور و هدر Referer
// ثبت می‌شوند. واریانت POST را بردارید.
//
// این مجموعه برای کاتلین سمت سرور است. کلاینت HTTP از خود JDK می‌آید و تنها
// وابستگی‌اش Gson است. برای اندروید examples/v3 را بردارید؛ بسته java.net.http
// روی اندروید وجود ندارد.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... ./run.sh examples/v3-jvm/send-token-single-get.kt

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
    val query = "ApiKey=" + encode(System.getenv("PAYAM_RESAN_API_KEY")) +
        "&TemplateKey=" + encode("verifycode") +
        "&Destination=" + encode("9121112222") +
        "&p1=" + encode("123456")

    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.sms-webservice.com/api/V3/SendTokenSingle?$query"))
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
        val message = item.asJsonObject
        println("شناسه ${message["Id"]}، متن نهایی: ${message["FinalText"].asString}")
    }
}
// docs:end
