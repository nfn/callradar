# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserva linha e ficheiro de origem para crash reports legíveis no Crashlytics
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Firebase Crashlytics — preserva exceções personalizadas
-keep public class * extends java.lang.Exception

# Google Mobile Ads SDK
-keep class com.google.android.gms.ads.** { *; }

# ── Gson ──────────────────────────────────────────────────────────────────────
# O Gson usa reflexão para deserializar JSON — sem estas regras o R8 renomeia
# os campos (entity → a, data → b) e a deserialização falha silenciosamente

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Mantém apenas os campos anotados que o Gson lê por reflexão.
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
