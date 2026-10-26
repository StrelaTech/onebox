plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.strela.onebox.ui"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
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
    // Прослойка: UI общается с ядрами только через VpnConnector (:api).
    api(project(":api"))
    // Модели и тексты у публичных сигнатур composables.
    api(project(":core-model"))
    api(project(":core-i18n"))
    implementation(project(":data"))
    implementation(project(":core-log"))
    // Диалог импорта вызывает детектор типов вставленного текста напрямую.
    implementation(project(":core-parsing"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
}
