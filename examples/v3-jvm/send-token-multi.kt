// SendTokenMulti - یک قالب، چند گیرنده، مقادیر متفاوت.
//
// پارامترها اینجا آرایه‌اند، نه p1 تا p10. درایه اول به {1} می‌نشیند، دومی به
// {2} و همین‌طور تا آخر: ترتیب از شماره جای‌گاه می‌آید، نه از جایی که در متن
// قالب دیده می‌شود.
//
// این مجموعه برای کاتلین سمت سرور است. کلاینت HTTP از خود JDK می‌آید و تنها
// وابستگی‌اش Gson است. برای اندروید examples/v3 را بردارید؛ بسته java.net.http
// روی اندروید وجود ندارد.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... ./run.sh examples/v3-jvm/send-token-multi.kt

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
    // قالب نمونه: «مرسوله شما از {2} تحویل پست شد. بارکد مرسوله پستی: {1}»
    val recipients = JsonArray()
    recipients.add(JsonObject().apply {
        addProperty("Destination", 9121112222L)
        addProperty("UserTraceId", 1001L)
        add("Parameters", JsonArray().apply {
            add("BARCODE-AAA")
            add("شیراز")
        })
    })
    recipients.add(JsonObject().apply {
        addProperty("Destination", 9121113333L)
        addProperty("UserTraceId", 1002L)
        add("Parameters", JsonArray().apply {
            add("BARCODE-BBB")
            add("تبریز")
        })
    })

    val payload = JsonObject().apply {
        addProperty("ApiKey", System.getenv("PAYAM_RESAN_API_KEY"))
        addProperty("TemplateKey", "postcode")
        add("Recipients", recipients)
    }

    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.sms-webservice.com/api/V3/SendTokenMulti"))
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
        println("${message["UserTraceId"]} => ${message["FinalText"].asString}")
    }
}
// docs:end
