plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Estes dois imports foram incluidos por mim para gerar o nome do ficheiro da build
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

android {
    namespace = "me.ligaram.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "me.ligaram.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0-beta.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}


// Estes modulo foi incluido por mim para gerar o nome do ficheiro da build
androidComponents {
    onVariants { variant ->
        // Captura a data uma vez por variante
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_hhmm"))

        variant.outputs.forEach { output ->
            val versionName = output.versionName.get() ?: "unknown"
            val fileName = "CallRadar-${versionName}-${timestamp}.apk"

            // Cast para a classe de implementação (necessário no AGP atual)
            (output as? com.android.build.api.variant.impl.VariantOutputImpl)?.let {
                it.outputFileName.set(fileName)
            }
        }
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
    debugImplementation(libs.androidx.ui.tooling)
}
