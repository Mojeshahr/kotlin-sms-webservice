<div align="center">

<a href="https://payam-resan.com">
  <img src=".github/assets/logo.svg" width="64" height="64" alt="پیام رسان">
</a>

<h1>نمونه‌کدهای Kotlin وب‌سرویس پیام رسان</h1>

اتصال به وب‌سرویس <a href="https://payam-resan.com"><b>پنل پیامکی پیام رسان</b></a> با Kotlin<br>
یک فایل قابل اجرا به‌ازای هر متد سرویس، برای اندروید و برای سرور

[![API](https://img.shields.io/badge/API-V3-0a7cbd)](https://payam-resan.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.8%2B-7f52ff)](https://kotlinlang.org)
[![Dependency](https://img.shields.io/badge/dependency-one%20per%20set-2ea44f)](#چرا-دو-مجموعه-نمونه)
[![License](https://img.shields.io/badge/license-MIT-6e7781)](LICENSE)

<b>فارسی</b> · <a href="README.en.md">English</a>

</div>

<sub>دنبال زبان دیگری هستید؟ همین نمونه‌ها برای زبان‌های دیگر هم در
[github.com/Mojeshahr](https://github.com/Mojeshahr) هست.</sub>

---

## شروع سریع

```bash
git clone https://github.com/Mojeshahr/kotlin-sms-webservice.git
cd kotlin-sms-webservice

./lib/get-jars.sh

export PAYAM_RESAN_API_KEY='123456-XXXXXXXXXXXXXXX'
export PAYAM_RESAN_SENDER='30004040'

./run.sh examples/v3-jvm/account-info.kt
```

با `account-info.kt` شروع کنید: چیزی ارسال نمی‌کند، اعتباری مصرف نمی‌کند، و
اگر جواب داد یعنی کلید و اتصال هر دو سالم‌اند.

فایل `run.sh` فقط برای همین مخزن است. کاتلین اجراکننده تک‌فایلی مثل `java`
ندارد، پس این اسکریپت فایل را کامپایل و اجرا می‌کند تا خود نمونه‌ها از
داربست ساخت خالی بمانند. هیچ نمونه‌ای به آن وابسته نیست.

## چرا دو مجموعه نمونه

کاتلین یک زبان است ولی دو پلتفرم دارد، و کتابخانه‌ای که روی یکی هست روی آن
یکی نیست:

<div dir="rtl">

| | `examples/v3` اندروید | `examples/v3-jvm` سرور |
|---|---|---|
| کلاینت HTTP | OkHttp | `java.net.http` از خود JDK |
| پارسر JSON | `org.json` از خود اندروید | Gson |
| وابستگی لازم | یکی، OkHttp | یکی، Gson |

</div>

دلیل اصلی این جدایی یک واقعیت است، نه سلیقه: **بسته `java.net.http` روی
اندروید وجود ندارد.** این API مال JDK 11 است و اندروید آن را ندارد؛ در فهرست
[desugaring گوگل](https://developer.android.com/studio/write/java11-minimal-support-table)
هم نیست. یعنی فایل سمت سرور روی اندروید اصلاً کامپایل نمی‌شود. در جهت عکس هم
`org.json` که در اندروید رایگان است، در JDK وجود ندارد.

هر مجموعه دقیقاً یک وابستگی می‌گیرد، همان قاعده‌ای که مخزن جاوا هم دارد.

## اندروید و کلید حساب

کلید حساب را داخل اپ نگذارید. هر رشته‌ای که در APK برود قابل استخراج است و
هر کسی که آن را دربیاورد با اعتبار شما پیامک می‌فرستد. نه در کد، نه در
`BuildConfig`، نه در `strings.xml`، نه در فایل `.so`.

جای درستش بک‌اند خودتان است. اپ به سرور شما می‌گوید «برای این کاربر کد
بفرست»، سرور شما تصمیم می‌گیرد و با کلید خودش این سرویس را صدا می‌زند. برای
همین توابع مجموعه اندروید کلید را **پارامتر** می‌گیرند و از متغیر محیطی
نمی‌خوانند: در اپ اندروید متغیر محیطی وجود ندارد.

نمونه‌های این مجموعه سرویس را مستقیم صدا می‌زنند تا شکل درخواست را نشان
بدهند. آن شکل همان است، چه از اپ صدایش بزنید چه از سرورتان؛ ولی در محصول
واقعی از سرورتان صدایش بزنید.

## اندروید و نخ اصلی

توابع مسدودکننده‌اند. اگر از نخ اصلی صدایشان بزنید،
`NetworkOnMainThreadException` می‌گیرید. تبدیلش به `suspend` دو خط است:

```kotlin
suspend fun accountInfoAsync(apiKey: String) = withContext(Dispatchers.IO) {
    accountInfo(apiKey)
}
```

این کار `kotlinx-coroutines` می‌خواهد، که در هر پروژه اندرویدی امروز هست.
داخل خود نمونه‌ها نیامده تا هر فایل با یک وابستگی کار کند.

## پیش از ارسال واقعی

یک سرور آزمایشی هست که مثل سرور عملیاتی جواب می‌دهد ولی پیامکی نمی‌فرستد و
اعتباری مصرف نمی‌کند. کافی است `V3` در نشانی را با `V3SandBox` عوض کنید. تنها
استثنا `TokenList` است که روی آن سرور پیاده نشده.

## متدها

<div dir="rtl">

| متد | اندروید | سرور | کار |
|---|---|---|---|
| `AccountInfo` | [account-info.kt](examples/v3/account-info.kt) | [account-info.kt](examples/v3-jvm/account-info.kt) | اعتبار و خطوط فعال |
| `Send` | [send.kt](examples/v3/send.kt) | [send.kt](examples/v3-jvm/send.kt) | ارسال ساده با `GET` |
| `SendBulk` | [send-bulk.kt](examples/v3/send-bulk.kt) | [send-bulk.kt](examples/v3-jvm/send-bulk.kt) | یک متن به چند گیرنده، با شناسه پی‌گیری |
| `SendMultiple` | [send-multiple.kt](examples/v3/send-multiple.kt) | [send-multiple.kt](examples/v3-jvm/send-multiple.kt) | متن جدا برای هر گیرنده |
| `TokenList` | [token-list.kt](examples/v3/token-list.kt) | [token-list.kt](examples/v3-jvm/token-list.kt) | فهرست قالب‌ها |
| `SendTokenSingle` | [send-token-single.kt](examples/v3/send-token-single.kt) | [send-token-single.kt](examples/v3-jvm/send-token-single.kt) | ارسال قالب به یک شماره |
| `SendTokenSingle` | [send-token-single-get.kt](examples/v3/send-token-single-get.kt) | [send-token-single-get.kt](examples/v3-jvm/send-token-single-get.kt) | همان، با `GET` |
| `SendTokenMulti` | [send-token-multi.kt](examples/v3/send-token-multi.kt) | [send-token-multi.kt](examples/v3-jvm/send-token-multi.kt) | یک قالب، چند گیرنده |
| `StatusById` | [status-by-id.kt](examples/v3/status-by-id.kt) | [status-by-id.kt](examples/v3-jvm/status-by-id.kt) | وضعیت با شناسه سامانه |
| `StatusByUserTraceId` | [status-by-user-trace-id.kt](examples/v3/status-by-user-trace-id.kt) | [status-by-user-trace-id.kt](examples/v3-jvm/status-by-user-trace-id.kt) | وضعیت با شناسه خودتان |
| `GetInbox` | [get-inbox.kt](examples/v3/get-inbox.kt) | [get-inbox.kt](examples/v3-jvm/get-inbox.kt) | پیامک‌های رسیده |

</div>

## استفاده در پروژه خودتان

نمونه‌ها عمداً به هیچ چیز این مخزن وابسته نیستند، پس کپی‌کردن فایل داخل پروژه
شما کافی است. به‌جای `lib/get-jars.sh` وابستگی را در Gradle تعریف کنید:

```kotlin
// اندروید
implementation("com.squareup.okhttp3:okhttp:4.12.0")

// سرور
implementation("com.google.code.gson:gson:2.11.0")
```

اگر در پروژه‌تان Retrofit دارید، OkHttp از قبل روی classpath هست و چیزی اضافه
نمی‌شود. اگر Ktor Client یا kotlinx.serialization دارید، لایه HTTP نمونه را
نبرید و فقط بدنه درخواست و بررسی `Success` را بردارید.

## چند نکته که وقت‌تان را می‌خرد

**کد وضعیت HTTP را نخوانید.** سرویس همیشه `200` برمی‌گرداند، حتی وقتی کلید
اشتباه است، پس `answer.isSuccessful` چیزی ثابت نمی‌کند. تصمیم را از فیلد
`Success` بگیرید. هر نمونه اینجا همین کار را می‌کند.

**شماره گیرنده صفر ابتدایی ندارد.** یعنی `9121112222` یا با کد کشور
`989121112222`. شماره‌ای که با `9` یا `989` شروع نشود کد خطای `13` می‌گیرد.

**متن را دوباره encode نکنید.** در نمونه اندروید `addQueryParameter` و در
نمونه سرور `URLEncoder` هرکدام خودشان یک بار این کار را می‌کنند. اگر پیش از
آن هم encode کنید، پیامک با نویسه‌های `%D8` به گوشی می‌رسد.

**برای هر گیرنده یک `UserTraceId` یکتا بفرستید.** بعد از یک timeout، این تنها
راه فهمیدن این است که پیامک ثبت شده یا نه.

## امنیت کلید

کلید یک راز است. در مخزن کد، در جاوااسکریپت مرورگر و در بسته اپلیکیشن موبایل
نباید قرار بگیرد. روی سرور جایش متغیر محیطی است و در موبایل باید از بک‌اند
خودتان بیاید.

اگر کلیدی لو رفت، از پنل یکی تازه بسازید. کلید حذف‌شده برنمی‌گردد.

## ساختار

<div dir="rtl">

| مسیر | چه چیزی دارد |
|---|---|
| `examples/v3/` | یک نمونه مستقل به‌ازای هر عملیات سرویس، برای اندروید |
| `examples/v3-jvm/` | همان‌ها برای کاتلین سمت سرور |
| `lib/get-jars.sh` | آوردن وابستگی هر مجموعه |
| `run.sh` | کامپایل و اجرای یک فایل |
| `.env.example` | نمونه متغیرهای محیطی |

</div>

عدد `v3` در مسیر عمدی است. نسخه تازه سرویس یعنی پوشه `examples/v<n>/` تازه، و
پوشه موجود دست‌نخورده می‌ماند.

## مستندات و پشتیبانی

راهنمای کامل وب‌سرویس در [docs.payam-resan.com](https://docs.payam-resan.com)
است. توصیف ماشین‌خوان OpenAPI هم در
[sms-webservice-spec](https://github.com/Mojeshahr/sms-webservice-spec).

سؤال یا خطایی هست؟ [issue باز کنید](https://github.com/Mojeshahr/kotlin-sms-webservice/issues)
یا با [پشتیبانی](https://payam-resan.com) تماس بگیرید.

## مجوز

منتشرشده با مجوز MIT. متن کامل در [`LICENSE`](LICENSE).

<br>
<div align="center">
  <sub>
    <img src=".github/assets/logo.svg" width="16" height="16" alt="" align="top">
    &nbsp;<b>پنل پیامکی پیام رسان - موج شهر</b>&nbsp;
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset=".github/assets/mojeshahr-dark.svg">
      <img src=".github/assets/mojeshahr-light.svg" width="16" height="16" alt="" align="top">
    </picture>
  </sub>
  <br>
  <sub>
    <a href="https://payam-resan.com">payam-resan.com</a>
    &nbsp;·&nbsp;
    <a href="https://mojeshahr.ir">mojeshahr.ir</a>
  </sub>
</div>
