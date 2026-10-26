plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.strela.onebox.vpn"
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
    // Прослойка: TunVpnServiceBridge реализует VpnServiceBridge, сервис зовёт VpnConnector.
    implementation(project(":api"))
    // Инициализация хранилищ в onStartCommand и строки уведомления (strings()).
    implementation(project(":data"))
    // SettingsStore.strings() отдаёт OneBoxStrings из :core-i18n.
    implementation(project(":core-i18n"))
    implementation(project(":core-log"))
    implementation(libs.androidx.core.ktx)
}
