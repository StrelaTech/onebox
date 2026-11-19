import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use(::load)
    }
}

android {
    namespace = "com.strela.onebox"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.strela.onebox"
        minSdk = 24
        targetSdk = 37
        versionCode = 260004
        versionName = "26.0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Нативные библиотеки только под целевые архитектуры. x86 не включаем:
        // это нужно только старым эмуляторам и добавляет ~32 МБ к универсальному APK.
        ndk {
            abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }
    }

    // Раздельные APK по ABI: в каждый попадает только своё нативное ядро
    // (libgojni.so для этой архитектуры). Плюс универсальный APK со всеми
    // тремя ядрами — для установки «одним файлом» и для эмулятора.
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86_64")
            isUniversalApk = true
        }
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            // Экспериментальный packageScope («постепенное внедрение R8») ломал
            // class merging: IllegalAccessError «Class kotlin.text.f extended by
            // class av0 is inaccessible» (и раньше — kotlin.sequences.d / sv0).
            // По документации packageScope не рекомендован, если R8 уже
            // используется: оптимизируем всё приложение целиком.
            optimization {
                enable = true
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core-log"))
    implementation(project(":core-model"))
    implementation(project(":data"))
    implementation(project(":engine"))
    implementation(project(":ui"))
    // SUI-кит: тема и компоненты, вынесенные в StrelaTech/SUI.
    implementation("com.strela.sui:kit")

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}