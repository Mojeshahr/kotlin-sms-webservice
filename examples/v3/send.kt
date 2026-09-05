// Send - ساده‌ترین ارسال، یک متن به چند شماره با یک درخواست GET.
//
// برای آزمایش سریع خوب است. در محیط عملیاتی SendBulk را بردارید: کلید را از
// نشانی بیرون می‌برد و برای هر گیرنده شناسه پی‌گیری می‌پذیرد.
//
// این مجموعه برای اندروید است. تنها وابستگی‌اش OkHttp است؛ org.json از خود
// پلتفرم می‌آید. برای کاتلین سمت سرور examples/v3-jvm را بردارید.
//
// کلید حساب را داخل اپ نگذارید. هنگام اجرا از بک‌اند خودتان بگیریدش. تابع هم
// مسدودکننده است و نباید از نخ اصلی صدا زده شود؛ شکل suspend در README آمده.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... PAYAM_RESAN_SENDER=... ./run.sh examples/v3/send.kt

// docs:start
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

fun send(apiKey: String, sender: Long) {
    // سازنده HttpUrl دقیقاً یک بار encode می‌کند. اگر متن را خودتان هم پیش از
    // این encode کنید، پیامک با نویسه‌های %D8 به گوشی می‌رسد.
    val url = "https://api.sms-webservice.com/api/V3/Send".toHttpUrl().newBuilder()
        .addQueryParameter("ApiKey", apiKey)
        .addQueryParameter("Sender", sender.toString())
        .addQueryParameter("Text", "کد تأیید شما ۱۲۳۴۵۶ است")
        .addQueryParameter("Recipients", "9121112222,9121113333")
        .build()

    val request = Request.Builder().url(url).build()

    OkHttpClient().newCall(request).execute().use { answer ->
        val response = JSONObject(answer.body!!.string())

        check(response.optBoolean("Success")) {
            "ناموفق. کد ${response.opt("ErrorCode")}: ${response.optString("Error")}"
        }

        val result = response.getJSONArray("Result")
        for (index in 0 until result.length()) {
            println("شناسه ${result.getJSONObject(index).get("Id")}")
        }
    }
}
// docs:end

// همین فایل روی دسکتاپ هم اجرا می‌شود تا ببینید سرویس چه برمی‌گرداند. در اپ
// واقعی این بخش وجود ندارد و کلید از بک‌اند خودتان می‌آید.
fun main() = send(System.getenv("PAYAM_RESAN_API_KEY"), System.getenv("PAYAM_RESAN_SENDER").toLong())
