pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Локальный Maven-репозиторий с xray.aar (gomobile bind).
        // Артефакт собирается scripts/build-xray-aar.sh и здесь подхватывается
        // как обычная зависимость — библиотечный модуль engine не может
        // напрямую зависеть от локального .aar.
        maven { url = uri("${rootDir}/local-maven") }
    }
}

rootProject.name = "OneBox"
include(":app", ":core", ":data", ":engine", ":ui")
