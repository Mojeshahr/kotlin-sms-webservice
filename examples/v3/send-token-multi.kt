// SendTokenMulti - یک قالب، چند گیرنده، مقادیر متفاوت.
//
// پارامترها اینجا آرایه‌اند، نه p1 تا p10. درایه اول به {1} می‌نشیند، دومی به
// {2} و همین‌طور تا آخر: ترتیب از شماره جای‌گاه می‌آید، نه از جایی که در متن
// قالب دیده می‌شود.
//
// این مجموعه برای اندروید است. تنها وابستگی‌اش OkHttp است؛ org.json از خود
// پلتفرم می‌آید. برای کاتلین سمت سرور examples/v3-jvm را بردارید.
//
// کلید حساب را داخل اپ نگذارید. هنگام اجرا از بک‌اند خودتان بگیریدش. تابع هم
// مسدودکننده است و نباید از نخ اصلی صدا زده شود؛ شکل suspend در README آمده.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... ./run.sh examples/v3/send-token-multi.kt

// docs:start
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

val JSON = "application/json; charset=utf-8".toMediaType()

fun sendTokenMulti(apiKey: String) {
    // قالب نمونه: «مرسوله شما از {2} تحویل پست شد. بارکد مرسوله پستی: {1}»
    val recipients = JSONArray()
        .put(
            JSONObject()
                .put("Destination", 9121112222L)
                .put("UserTraceId", 1001L)
                .put("Parameters", JSONArray().put("BARCODE-AAA").put("شیراز"))
        )
        .put(
            JSONObject()
                .put("Destination", 9121113333L)
                .put("UserTraceId", 1002L)
                .put("Parameters", JSONArray().put("BARCODE-BBB").put("تبریز"))
        )

    val payload = JSONObject()
        .put("ApiKey", apiKey)
        .put("TemplateKey", "postcode")
        .put("Recipients", recipients)

    val request = Request.Builder()
        .url("https://api.sms-webservice.com/api/V3/SendTokenMulti")
        .post(payload.toString().toRequestBody(JSON))
        .build()

    OkHttpClient().newCall(request).execute().use { answer ->
        val response = JSONObject(answer.body!!.string())

        check(response.optBoolean("Success")) {
            "ناموفق. کد ${response.opt("ErrorCode")}: ${response.optString("Error")}"
        }

        val result = response.getJSONArray("Result")
        for (index in 0 until result.length()) {
            val message = result.getJSONObject(index)
            println("${message.get("UserTraceId")} => ${message.getString("FinalText")}")
        }
    }
}
// docs:end

// همین فایل روی دسکتاپ هم اجرا می‌شود تا ببینید سرویس چه برمی‌گرداند. در اپ
// واقعی این بخش وجود ندارد و کلید از بک‌اند خودتان می‌آید.
fun main() = sendTokenMulti(System.getenv("PAYAM_RESAN_API_KEY"))
