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
        // как обычная зависимость — библиотечный модуль engines/xray не может
        // напрямую зависеть от локального .aar.
        maven { url = uri("${rootDir}/local-maven") }
    }
}

rootProject.name = "OneBox"

// Приложение и оболочка UI.
include(":app")
include(":ui")

// Прослоечный слой: единственная точка контакта UI с ядрами VPN.
include(":api")

// Вынесенные из прежнего монолита :core модули доменных частей.
include(":core-i18n")
include(":core-model")
include(":core-parsing")
include(":core-log")
project(":core-i18n").projectDir = file("core/i18n")
project(":core-model").projectDir = file("core/model")
project(":core-parsing").projectDir = file("core/parsing")
project(":core-log").projectDir = file("core/log")

// Данные (хранилища, загрузка подписок) и исполнительная среда.
include(":data")
include(":engine")

// Системный VpnService (TunVpnService, TunVpnServiceBridge) — вынесен из :engine.
include(":vpn")

// Ядра VPN: каждая реализация прослойки (:api.VpnCore) — отдельный модуль.
include(":xray")
project(":xray").projectDir = file("engines/xray")
