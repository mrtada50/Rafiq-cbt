# رفيق — تطبيق أندرويد (Kotlin + Jetpack Compose)

تطبيق أندرويد أصلي كامل بأدوات العلاج المعرفي السلوكي، مبني بـ Kotlin وJetpack Compose وRoom،
ويُبنى تلقائيًا إلى ملف APK عبر GitHub Actions.

## المكدس التقني
- **Kotlin** لغة أساسية
- **Jetpack Compose** لواجهة المستخدم (بدل XML)
- **Room** لقاعدة بيانات محلية دائمة على الجهاز
- **Gradle (Kotlin DSL)** لإدارة البناء
- **GitHub Actions** لبناء APK تلقائيًا بدون الحاجة لأندرويد ستوديو

## الميزات
مزاج، سجل أفكار + تشوهات فكرية + سهم هابط، تنشيط سلوكي، تجارب سلوكية، سلّم تعرّض تدريجي،
حل مشكلات، وقت مخصص للقلق، تمرين تنفس، تأريض 5-4-3-2-1، استرخاء عضلي تدريجي، خطة وقاية من الانتكاس.

---

## خطوات الاستخدام من Termux + GitHub

### 1) تجهيز Termux
```bash
pkg update && pkg upgrade -y
pkg install git openssh -y
```

### 2) إعداد هويتك بـ git (مرة وحدة بس)
```bash
git config --global user.name "اسمك"
git config --global user.email "بريدك@example.com"
```

### 3) إنشاء مستودع فاضي على GitHub
سوّي مستودع جديد من موقع GitHub (بدون README)، وخذ رابطه، مثلاً:
`https://github.com/USERNAME/rafiq-android.git`

### 4) رفع المشروع من Termux
انسخ مجلد المشروع هذا إلى جهازك (فك ضغط الملف اللي انبعث إلك)، بعدين من داخل مجلد المشروع:
```bash
cd rafiq-android
git init
git add .
git commit -m "أول نسخة من تطبيق رفيق"
git branch -M main
git remote add origin https://github.com/USERNAME/rafiq-android.git
git push -u origin main
```
إذا طلب منك مصادقة، استخدم **Personal Access Token** بدل الباسورد العادي (من إعدادات GitHub → Developer settings → Personal access tokens).

### 5) البناء التلقائي
بمجرد ما تدزّ (push) الكود، بيشتغل GitHub Actions تلقائيًا ويبني ملف APK.
تابع التقدّم من تبويب **Actions** بمستودعك على GitHub.

### 6) تحميل الـ APK
بعد ما يخلص البناء (يأخذ عادة بضع دقائق):
1. افتح تبويب **Actions** بالمستودع
2. اضغط على آخر تشغيل (run) ناجح
3. تحت **Artifacts** بترة **rafiq-debug-apk** — حمّلها (ملف zip يحتوي app-debug.apk)
4. انقل الـ APK لهاتفك وثبّته (لازم تفعّل "تثبيت من مصادر غير معروفة" بإعدادات الأندرويد)

### تحديثات لاحقة
أي تعديل بالكود، بس سوّي:
```bash
git add .
git commit -m "وصف التعديل"
git push
```
وبيبني APK جديد تلقائيًا.

---

## ملاحظة تقنية
النسخ (Kotlin 1.9.22 / AGP 8.2.2 / Compose compiler 1.5.8 / Room 2.6.1) مجرّبة ومتوافقة مع بعضها وقت كتابة هذا المشروع.
إذا صار خطأ توافق إصدارات بسيط بالبناء (نادر لكن ممكن مع تحديثات Google المستقبلية)، افتح لوگ الخطأ بتبويب Actions
وبيوضح بالضبط أي إصدار يحتاج تعديل — عدّل الرقم بملف `app/build.gradle.kts` أو `build.gradle.kts` الجذري وادفع من جديد.

## البنية
```
app/src/main/kotlin/com/rafiq/cbt/
├── data/            # Room entities + DAO + قاعدة البيانات + البيانات الثابتة (التشوهات، خطوات التأريض...)
├── ui/theme/        # الألوان والتصميم
├── ui/components/   # مكونات Compose قابلة لإعادة الاستخدام
├── ui/screens/      # شاشات كل أداة
├── AppViewModel.kt  # منطق التطبيق وربطه بقاعدة البيانات
├── CbtApplication.kt
└── MainActivity.kt
```
