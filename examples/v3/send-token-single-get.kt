// SendTokenSingle با GET - همان ارسال قالب، با ورودی در نشانی.
//
// برای آزمایش دستی مناسب است، برای محیط عملیاتی نه: در GET هم کلید حساب و هم
// مقدار رمز یک‌بارمصرف داخل نشانی می‌نشینند و در لاگ وب‌سرور و هدر Referer
// ثبت می‌شوند. واریانت POST را بردارید.
//
// این مجموعه برای اندروید است. تنها وابستگی‌اش OkHttp است؛ org.json از خود
// پلتفرم می‌آید. برای کاتلین سمت سرور examples/v3-jvm را بردارید.
//
// رمز یک‌بارمصرف را از خود اپ نفرستید. جای این کار بک‌اند شماست.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... ./run.sh examples/v3/send-token-single-get.kt

// docs:start
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

fun sendTokenSingleByQuery(apiKey: String) {
    val url = "https://api.sms-webservice.com/api/V3/SendTokenSingle".toHttpUrl().newBuilder()
        .addQueryParameter("ApiKey", apiKey)
        .addQueryParameter("TemplateKey", "verifycode")
        .addQueryParameter("Destination", "9121112222")
        .addQueryParameter("p1", "123456")
        .build()

    val request = Request.Builder().url(url).build()

    OkHttpClient().newCall(request).execute().use { answer ->
        val response = JSONObject(answer.body!!.string())

        check(response.optBoolean("Success")) {
            "ناموفق. کد ${response.opt("ErrorCode")}: ${response.optString("Error")}"
        }

        val result = response.getJSONArray("Result")
        for (index in 0 until result.length()) {
            val message = result.getJSONObject(index)
            println("شناسه ${message.get("Id")}، متن نهایی: ${message.getString("FinalText")}")
        }
    }
}
// docs:end

// همین فایل روی دسکتاپ هم اجرا می‌شود تا ببینید سرویس چه برمی‌گرداند. در اپ
// واقعی این بخش وجود ندارد و کلید از بک‌اند خودتان می‌آید.
fun main() = sendTokenSingleByQuery(System.getenv("PAYAM_RESAN_API_KEY"))
