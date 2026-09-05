// AccountInfo - اعتبار باقی‌مانده و خطوط فعال حساب.
//
// سبک‌ترین متد سرویس و بهترین راه آزمودن کلید: چیزی ارسال نمی‌کند، اعتباری
// مصرف نمی‌کند، و حتی با اعتبار صفر هم جواب می‌دهد.
//
// این مجموعه برای اندروید است. تنها وابستگی‌اش OkHttp است؛ org.json از خود
// پلتفرم اندروید می‌آید و چیزی به build.gradle اضافه نمی‌کند. برای کاتلین سمت
// سرور examples/v3-jvm را بردارید.
//
// کلید حساب را داخل اپ نگذارید. هر کلیدی که در APK برود قابل استخراج است و با
// اعتبار شما پیامک فرستاده می‌شود. کلید را هنگام اجرا از بک‌اند خودتان بگیرید
// و به این تابع بدهید. شرحش در README آمده.
//
// تابع مسدودکننده است و نباید از نخ اصلی صدا زده شود. شکل suspend آن هم در
// README هست.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... ./run.sh examples/v3/account-info.kt

// docs:start
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

val JSON = "application/json; charset=utf-8".toMediaType()

fun accountInfo(apiKey: String) {
    val payload = JSONObject().put("ApiKey", apiKey)

    val request = Request.Builder()
        .url("https://api.sms-webservice.com/api/V3/AccountInfo")
        .post(payload.toString().toRequestBody(JSON))
        .build()

    OkHttpClient().newCall(request).execute().use { answer ->
        val response = JSONObject(answer.body!!.string())

        check(response.optBoolean("Success")) {
            "ناموفق. کد ${response.opt("ErrorCode")}: ${response.optString("Error")}"
        }

        val result = response.getJSONObject("Result")
        println("اعتبار: ${result.getDouble("Credit")}")

        val lines = result.getJSONArray("AvailableSenders")
        for (index in 0 until lines.length()) {
            println("خط: ${lines.get(index)}")
        }
    }
}
// docs:end

// همین فایل روی دسکتاپ هم اجرا می‌شود تا ببینید سرویس چه برمی‌گرداند. در اپ
// واقعی این بخش وجود ندارد و کلید از بک‌اند خودتان می‌آید.
fun main() = accountInfo(System.getenv("PAYAM_RESAN_API_KEY"))
