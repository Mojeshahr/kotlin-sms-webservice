// SendBulk - یک متن به چند گیرنده، هر کدام با شناسه پی‌گیری خودتان.
//
// روش پیشنهادی برای ارسال عملیاتی. کلید در بدنه درخواست می‌رود نه در نشانی،
// و برای هر گیرنده UserTraceId می‌پذیرد تا گزارش تحویل را بدون نگه‌داشتن Id
// سامانه بگیرید.
//
// این مجموعه برای اندروید است. تنها وابستگی‌اش OkHttp است؛ org.json از خود
// پلتفرم می‌آید. برای کاتلین سمت سرور examples/v3-jvm را بردارید.
//
// کلید حساب را داخل اپ نگذارید. هنگام اجرا از بک‌اند خودتان بگیریدش. تابع هم
// مسدودکننده است و نباید از نخ اصلی صدا زده شود؛ شکل suspend در README آمده.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... PAYAM_RESAN_SENDER=... ./run.sh examples/v3/send-bulk.kt

// docs:start
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

val JSON = "application/json; charset=utf-8".toMediaType()

fun sendBulk(apiKey: String, sender: Long) {
    val recipients = JSONArray()
        .put(JSONObject().put("Destination", 9121112222L).put("UserTraceId", 1001L))
        .put(JSONObject().put("Destination", 9121113333L).put("UserTraceId", 1002L))

    val payload = JSONObject()
        .put("ApiKey", apiKey)
        .put("Sender", sender)
        .put("Text", "سفارش شما ثبت شد.")
        .put("Recipients", recipients)

    val request = Request.Builder()
        .url("https://api.sms-webservice.com/api/V3/SendBulk")
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
            println("${message.get("UserTraceId")} => شناسه ${message.get("Id")}")
        }
    }
}
// docs:end

// همین فایل روی دسکتاپ هم اجرا می‌شود تا ببینید سرویس چه برمی‌گرداند. در اپ
// واقعی این بخش وجود ندارد و کلید از بک‌اند خودتان می‌آید.
fun main() = sendBulk(System.getenv("PAYAM_RESAN_API_KEY"), System.getenv("PAYAM_RESAN_SENDER").toLong())
