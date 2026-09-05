// SendTokenSingle - ارسال قالب به یک شماره، با بدنه JSON.
//
// مسیر معمول رمز یک‌بارمصرف. خط فرستنده ورودی ندارد؛ سامانه آن را از روی خود
// قالب برمی‌دارد. همین واریانت POST را به کار ببرید، نه GET: در GET هم کلید
// حساب و هم خود رمز داخل نشانی و لاگ وب‌سرور می‌نشینند.
//
// این مجموعه برای کاتلین سمت سرور است. کلاینت HTTP از خود JDK می‌آید و تنها
// وابستگی‌اش Gson است. برای اندروید examples/v3 را بردارید؛ بسته java.net.http
// روی اندروید وجود ندارد.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... ./run.sh examples/v3-jvm/send-token-single.kt

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
    val payload = JsonObject().apply {
        addProperty("ApiKey", System.getenv("PAYAM_RESAN_API_KEY"))
        addProperty("TemplateKey", "verifycode")
        addProperty("Destination", 9121112222L)
        addProperty("p1", "123456")
    }

    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.sms-webservice.com/api/V3/SendTokenSingle"))
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

    // این متد UserTraceId در ورودی ندارد، پس در پاسخ null برمی‌گردد. اگر شناسه
    // پی‌گیری لازم دارید، SendTokenMulti را حتی برای یک گیرنده هم می‌شود به کار برد.
    for (item in response.getAsJsonArray("Result")) {
        val message = item.asJsonObject
        println("شناسه ${message["Id"]} از خط ${message["Sender"]}")
        println("متن نهایی: ${message["FinalText"].asString}")
    }
}
// docs:end
