// Estes dois imports foram incluidos por mim para gerar o nome do ficheiro da build
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "me.ligaram.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "me.ligaram.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 10000
        versionName = "1.0.0"
    }

    buildTypes {
        debug {
            buildConfigField("String", "ADMOB_NATIVE_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/2247696110\"") // ID de teste Google
        }
        release {
            // isMinifyEnabled = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "ADMOB_NATIVE_AD_UNIT_ID", "\"ca-app-pub-5751026474053499/4297086853\"") // TODO: substituir pelo Ad Unit ID real
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.accompanist.permissions)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.androidx.splashscreen)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.play.services.ads)
    debugImplementation(libs.androidx.ui.tooling)
}

// Estes modulo foi incluido por mim para gerar o nome do ficheiro da build
androidComponents {
    onVariants { variant ->

        val timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))

        variant.outputs.forEach { output ->
            val versionName = output.versionName.get()
            val fileName = "CallRadar-${versionName}-${timestamp}.apk"

            (output as? com.android.build.api.variant.impl.VariantOutputImpl)
                ?.outputFileName
                ?.set(fileName)
        }
    }
}