# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ── Gson ──────────────────────────────────────────────────────────────────────
# O Gson usa reflexão para deserializar JSON — sem estas regras o R8 renomeia
# os campos (entity → a, data → b) e a deserialização falha silenciosamente

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }

# ── Modelos de dados (usados pelo Gson via reflexão) ──────────────────────────
-keep class me.ligaram.app.data.** { *; }
-keepclassmembers class me.ligaram.app.data.** {
    <fields>;
    <init>(...);
}