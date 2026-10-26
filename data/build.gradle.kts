plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.strela.onebox.data"
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
}

dependencies {
    // Прослойка: StoreProfileSource реализует ProfileSource из :api.
    implementation(project(":api"))
    implementation(project(":core-model"))
    implementation(project(":core-parsing"))
    implementation(project(":core-i18n"))
    implementation(project(":core-log"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.okhttp)
}
