// SendMultiple - متن و خط فرستنده جدا برای هر گیرنده.
//
// برای پیام‌های شخصی‌سازی‌شده که با یک قالب ثابت پوشش داده نمی‌شوند. برخلاف
// SendBulk، اینجا Text و Sender در سطح هر گیرنده تعریف می‌شوند.
//
// این مجموعه برای اندروید است. تنها وابستگی‌اش OkHttp است؛ org.json از خود
// پلتفرم می‌آید. برای کاتلین سمت سرور examples/v3-jvm را بردارید.
//
// کلید حساب را داخل اپ نگذارید. هنگام اجرا از بک‌اند خودتان بگیریدش. تابع هم
// مسدودکننده است و نباید از نخ اصلی صدا زده شود؛ شکل suspend در README آمده.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... PAYAM_RESAN_SENDER=... ./run.sh examples/v3/send-multiple.kt

// docs:start
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

val JSON = "application/json; charset=utf-8".toMediaType()

fun sendMultiple(apiKey: String, sender: Long) {
    val recipients = JSONArray()
        .put(
            JSONObject()
                .put("Sender", sender)
                .put("Destination", 9121112222L)
                .put("Text", "آقای محمدی، سفارش شما ارسال شد.")
                .put("UserTraceId", 1001L)
        )
        .put(
            JSONObject()
                .put("Sender", sender)
                .put("Destination", 9121113333L)
                .put("Text", "خانم رضایی، سفارش شما ارسال شد.")
                .put("UserTraceId", 1002L)
        )

    val payload = JSONObject()
        .put("ApiKey", apiKey)
        .put("Recipients", recipients)

    val request = Request.Builder()
        .url("https://api.sms-webservice.com/api/V3/SendMultiple")
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
fun main() = sendMultiple(System.getenv("PAYAM_RESAN_API_KEY"), System.getenv("PAYAM_RESAN_SENDER").toLong())
