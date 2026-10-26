// Чистые тексты интерфейса — без Android; провайдер Compose живёт в :ui.
// JVM-модуль: компилируется быстрее библиотечного и не тянет android.jar.
plugins {
    // Версию не указываем: Kotlin-плагин уже в classpath через корневой
    // build.gradle.kts (kotlin-compose), иначе Gradle ругается на двойную загрузку.
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

// Целевой байт-код 11 — согласован с Java-целью библиотек Android;
// toolchain 11 подтягивается через foojay-resolver при необходимости.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}
