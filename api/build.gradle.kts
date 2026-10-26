plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.strela.onebox.api"
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
    // Server/Subscription у публичных сигнатур (VpnCore.pingServer и т.д.)
    api(project(":core-model"))
    implementation(project(":core-log"))
    implementation(libs.kotlinx.coroutines.core)
}
