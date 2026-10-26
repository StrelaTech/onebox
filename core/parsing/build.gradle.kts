plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.strela.onebox.parsing"
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(project(":core-model"))
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
}

// Robolectric 4.17 требует Java 21 (на Java 25 падает FileDescriptor-интерсептор),
// тогда как сборка идёт на Java 25 — для тестов используем отдельный тулчейн.
// Плюс модулю java.base нужен доступ к внутренним классам (иначе падает интерсептор).
tasks.withType<Test>().configureEach {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        },
    )
    jvmArgs(
        "--add-exports", "java.base/jdk.internal.access=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
        "--add-opens", "java.base/java.io=ALL-UNNAMED",
        "--add-opens", "java.base/java.util=ALL-UNNAMED",
    )
}
