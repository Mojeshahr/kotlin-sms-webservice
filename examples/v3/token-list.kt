// TokenList - قالب‌های حساب، با کلید و متن و وضعیت تأییدشان.
//
// برای پیدا کردن TemplateKey که متدهای ارسال قالب لازم دارند. این متد هم مثل
// AccountInfo از بررسی اعتبار معاف است.
//
// این مجموعه برای اندروید است. تنها وابستگی‌اش OkHttp است؛ org.json از خود
// پلتفرم می‌آید. برای کاتلین سمت سرور examples/v3-jvm را بردارید.
//
// کلید حساب را داخل اپ نگذارید. هنگام اجرا از بک‌اند خودتان بگیریدش. تابع هم
// مسدودکننده است و نباید از نخ اصلی صدا زده شود؛ شکل suspend در README آمده.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... ./run.sh examples/v3/token-list.kt

// docs:start
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

val JSON = "application/json; charset=utf-8".toMediaType()

fun tokenList(apiKey: String) {
    val payload = JSONObject().put("ApiKey", apiKey)

    val request = Request.Builder()
        .url("https://api.sms-webservice.com/api/V3/TokenList")
        .post(payload.toString().toRequestBody(JSON))
        .build()

    OkHttpClient().newCall(request).execute().use { answer ->
        val response = JSONObject(answer.body!!.string())

        check(response.optBoolean("Success")) {
            "ناموفق. کد ${response.opt("ErrorCode")}: ${response.optString("Error")}"
        }

        val result = response.getJSONArray("Result")
        for (index in 0 until result.length()) {
            val template = result.getJSONObject(index)
            val sendable = if (template.getInt("Status") == 2) "قابل ارسال" else "قابل ارسال نیست"
            println("${template.getString("Key")} ($sendable): ${template.getString("TextTemplate")}")
        }
    }
}
// docs:end

// همین فایل روی دسکتاپ هم اجرا می‌شود تا ببینید سرویس چه برمی‌گرداند. در اپ
// واقعی این بخش وجود ندارد و کلید از بک‌اند خودتان می‌آید.
fun main() = tokenList(System.getenv("PAYAM_RESAN_API_KEY"))
