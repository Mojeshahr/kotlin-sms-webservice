// SendTokenSingle - ارسال قالب به یک شماره، با بدنه JSON.
//
// مسیر معمول رمز یک‌بارمصرف. خط فرستنده ورودی ندارد؛ سامانه آن را از روی خود
// قالب برمی‌دارد. همین واریانت POST را به کار ببرید، نه GET: در GET هم کلید
// حساب و هم خود رمز داخل نشانی و لاگ وب‌سرور می‌نشینند.
//
// این مجموعه برای اندروید است. تنها وابستگی‌اش OkHttp است؛ org.json از خود
// پلتفرم می‌آید. برای کاتلین سمت سرور examples/v3-jvm را بردارید.
//
// رمز یک‌بارمصرف را از خود اپ نفرستید. اپ نباید کلید حساب را داشته باشد و
// نباید تعیین کند چه کدی برای چه شماره‌ای می‌رود؛ جای این کار بک‌اند شماست.
//
//   ./lib/get-jars.sh
//   PAYAM_RESAN_API_KEY=... ./run.sh examples/v3/send-token-single.kt

// docs:start
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

val JSON = "application/json; charset=utf-8".toMediaType()

fun sendTokenSingle(apiKey: String) {
    val payload = JSONObject()
        .put("ApiKey", apiKey)
        .put("TemplateKey", "verifycode")
        .put("Destination", 9121112222L)
        .put("p1", "123456")

    val request = Request.Builder()
        .url("https://api.sms-webservice.com/api/V3/SendTokenSingle")
        .post(payload.toString().toRequestBody(JSON))
        .build()

    OkHttpClient().newCall(request).execute().use { answer ->
        val response = JSONObject(answer.body!!.string())

        check(response.optBoolean("Success")) {
            "ناموفق. کد ${response.opt("ErrorCode")}: ${response.optString("Error")}"
        }

        // این متد UserTraceId در ورودی ندارد، پس در پاسخ null برمی‌گردد. اگر
        // شناسه پی‌گیری لازم دارید، SendTokenMulti را حتی برای یک گیرنده هم
        // می‌شود به کار برد.
        val result = response.getJSONArray("Result")
        for (index in 0 until result.length()) {
            val message = result.getJSONObject(index)
            println("شناسه ${message.get("Id")} از خط ${message.get("Sender")}")
            println("متن نهایی: ${message.getString("FinalText")}")
        }
    }
}
// docs:end

// همین فایل روی دسکتاپ هم اجرا می‌شود تا ببینید سرویس چه برمی‌گرداند. در اپ
// واقعی این بخش وجود ندارد و کلید از بک‌اند خودتان می‌آید.
fun main() = sendTokenSingle(System.getenv("PAYAM_RESAN_API_KEY"))
