// StatusByUserTraceId - وضعیت پیامک با شناسه‌هایی که خودتان داده‌اید.
//
// اگر UserTraceId را کلید رکورد پایگاه داده خودتان بگذارید، دیگر لازم نیست Id
// سامانه را ذخیره کنید. این متد راه امن تشخیص ارسال تکراری هم هست: بعد از قطع
// ارتباط، اول اینجا بپرسید ثبت شده یا نه.
//
// این مجموعه برای اندروید است. تنها وابستگی‌اش OkHttp است؛ org.json از خود
// پلتفرم می‌آید. برای کاتلین سمت سرور examples/v3-jvm را بردارید.
//
// کلید حساب را داخل اپ نگذارید. هنگام اجرا از بک‌اند خودتان بگیریدش. تابع هم
// مسدودکننده است و نباید از نخ اصلی صدا زده شود؛ شکل suspend در README آمده.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... ./run.sh examples/v3/status-by-user-trace-id.kt

// docs:start
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

val JSON = "application/json; charset=utf-8".toMediaType()

fun statusByUserTraceId(apiKey: String) {
    val payload = JSONObject()
        .put("ApiKey", apiKey)
        .put("UserTraceIds", JSONArray().put(1001L).put(1002L))

    val request = Request.Builder()
        .url("https://api.sms-webservice.com/api/V3/StatusByUserTraceId")
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

            // کد ۸ یعنی این شناسه در حساب شما نیست. بعد از یک timeout، همین
            // یعنی ارسال ثبت نشده و می‌توانید با خیال راحت دوباره بفرستید.
            if (message.getInt("StatusCode") == 8) {
                println("${message.get("UserTraceId")}: ثبت نشده")
                continue
            }

            println("${message.get("UserTraceId")}: ${message.getString("Status")}")
        }
    }
}
// docs:end

// همین فایل روی دسکتاپ هم اجرا می‌شود تا ببینید سرویس چه برمی‌گرداند. در اپ
// واقعی این بخش وجود ندارد و کلید از بک‌اند خودتان می‌آید.
fun main() = statusByUserTraceId(System.getenv("PAYAM_RESAN_API_KEY"))
