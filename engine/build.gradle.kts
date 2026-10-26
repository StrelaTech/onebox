plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.strela.onebox.engine"
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
    // VpnConnector-фасад и другие типы прослойки доступны потребителям (:app).
    api(project(":api"))
    // Системный VpnService: TunVpnService и его мост, зарегистрированные ниже.
    implementation(project(":vpn"))
    // Ядро xray — одна из реализаций VpnCore; само по нейтральному сервису не нужно.
    implementation(project(":xray"))
    implementation(project(":data"))
}
