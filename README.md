<div align="center">

# OneBox

**VPN-клиент для Android на xray-core — подписка, серверы и подключение в одном приложении.**

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)
![Android 7.0+](https://img.shields.io/badge/Android-7.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2026.02-4285F4?logo=jetpackcompose&logoColor=white)
![Version](https://img.shields.io/badge/Version-26.0.1.1--alpha-orange)

[Возможности](#-возможности) · [Быстрый старт](#-быстрый-старт) · [Скачать](#-скачать) · [Сборка](#-сборка-из-исходников) · [Архитектура](#-архитектура) · [Релизы](#-релизы)

</div>

---

OneBox — свободный VPN-клиент для Android, работающий через [xray-core](https://github.com/XTLS/Xray-core). Вставьте ссылку на подписку вашего провайдера — приложение скачает список серверов, покажет срок действия и остаток трафика, позволит проверить задержку и подключиться в одно нажатие.

Приложение не требует аккаунта, не содержит рекламы и аналитики. Подписки хранятся только на устройстве. Код распространяется по лицензии [GPL-3.0](LICENSE).

## ✨ Возможности

### 📡 Подписки и серверы

- **Импорт подписки по `https://`** — каждый URL становится отдельной подпиской. Повторная вставка того же URL обновляет её, а не создаёт дубль.
- **Метаданные из заголовков** — название, описание, дата окончания, использованный трафик и лимит (`Profile-Title`, `Announce`, `Subscription-Userinfo`).
- **Гибкий ручной импорт** — ссылки `vless://`, `vmess://`, `trojan://`, `ss://`, `socks://`, списки ссылок, Base64-подписки и готовые JSON-конфиги xray-core. Формат определяется автоматически.
- **Несколько подписок** — можно хранить сколько угодно профилей, переключаться между ними и удалять отдельные серверы.
- **Проверка задержки** — ping отдельного сервера или всех серверов подписки.
- **Только `https://`** — незащищённые подписки с учётными данными отклоняются.

### 🔌 Подключение

- **Системный `VpnService`** — трафик идёт через `tun`-интерфейс, VPN работает для всех приложений.
- **Одно нажатие** — подключение и отключение с главного экрана, постоянное уведомление о статусе.
- **Автоподключение** — запуск VPN при старте приложения, если разрешение уже выдано.
- **Блокировка рекламы** — рекламные и трекинговые домены уходят в `blackhole`; отключается в настройках.
- **GeoIP/GeoSite** — `geoip.dat` и `geosite.dat` поставляются в APK и распаковываются при первом запуске.

### 🎨 Интерфейс

- **Jetpack Compose** — собственный дизайн-кит без Material Design.
- **Тёмная, светлая и системная тема.**
- **Русский и английский язык** — переключается внутри приложения.
- **Три варианта иконки** — классическая, минималистичная и цветная.
- **Диагностика** — файл журнала с возможностью поделиться и очистить.
- **Сброс настроек** одним действием.

## 📋 Поддерживаемые протоколы

| Протокол | Ссылка | Транспорты и безопасность |
|----------|--------|---------------------------|
| **VLESS** | `vless://` | TCP, WebSocket, gRPC, HTTPUpgrade, XHTTP; TLS, Reality |
| **VMess** | `vmess://` | TCP, WebSocket, gRPC, HTTPUpgrade, XHTTP; TLS |
| **Trojan** | `trojan://` | TCP, WebSocket, gRPC, HTTPUpgrade, XHTTP; TLS |
| **Shadowsocks** | `ss://` | SIP002 и legacy-формат |
| **SOCKS** | `socks://` | SOCKS5, в т.ч. с логином и паролем |
| **JSON-конфиг** | файл/строка | Любой конфиг, который понимает xray-core |

## 🚀 Быстрый старт

1. **Установите APK.** Скачайте последний файл `OneBox-*.apk` со [страницы Releases](https://github.com/StrelaTech/onebox/releases) и установите его. Потребуется разрешение «Установить из неизвестных источников». Минимальная версия — Android 7.0 (API 24).
2. **Добавьте подписку.** Откройте вкладку **«Серверы»** → **«Добавить»** и вставьте ссылку от провайдера. Если подписки нет, можно вставить отдельную ссылку на сервер, Base64-строку или JSON-конфиг — формат определится сам.
3. **Проверьте сервер.** Нажмите кнопку ping рядом с сервером или «Проверить все», чтобы выбрать быстрый.
4. **Подключитесь.** На вкладке **«Главная»** нажмите «Подключиться» и подтвердите системный запрос на создание VPN-соединения. Это стандартный диалог Android; без вашего согласия приложение не создаёт туннель.
5. **Настройте под себя.** Во вкладке **«Настройки»** можно сменить тему, язык, иконку, включить автоподключение или блокировку рекламы.

Чтобы обновить серверы подписки, нажмите кнопку обновления на её карточке. Подписка обновляется по запросу, а не в фоне.

## 📥 Скачать

Последняя версия: **26.0.1.1-alpha**

| Файл | SHA256 |
|------|--------|
| [`OneBox-26.0.1.1-alpha.apk`](dist/OneBox-26.0.1.1-alpha.apk) | `0d95ee6af10b15d6859a6fc238adcaed5f089e4c91bed4e381cb7bece4195f95` |
| [`OneBox-26.0.1.1-alpha.aab`](dist/OneBox-26.0.1.1-alpha.aab) | `a6e5ec9239ea24541fc340ae13fbb5a2f7d722354feca3271af375965a83ca1a` |

Полные контрольные суммы: [`dist/SHA256SUMS.txt`](dist/SHA256SUMS.txt)

> **Примечание:** GitHub Actions не используется — релизы собираются локально и прикрепляются к тегам вручную.

---

# 🛠 Для разработчиков

## Технологии

- **Kotlin 2.3**, **Jetpack Compose** (Compose Foundation + собственный дизайн-кит)
- **AGP 9.4**, **Gradle 9.6**, **JDK 25** для сборки
- **xray-core** через Go-обёртку и `gomobile bind`
- **OkHttp** для загрузки подписок
- **Robolectric + JUnit 4** для unit-тестов парсинга и сборки конфига

## Архитектура

Проект разделён на модули. UI не знает о конкретном VPN-ядре: он общается только с прослойкой `:api`, а ядра реализуют её SPI и регистрируются при старте приложения.

```
app
 ├── ui ──────────────► api ◄────────── engines/xray
 │                       ▲                    │
 │                       │                    └── xray.aar (gomobile)
 ├── data ───────────────┤
 │   ├── core-model      │
 │   ├── core-parsing    │
 │   ├── core-i18n       │
 │   └── core-log        │
 ├── engine ─────────────┘
 │   ├── engines/xray
 │   └── vpn
 └── vpn ───────────────► api
```

| Модуль | Тип | Назначение |
|--------|-----|------------|
| `:app` | application | Точка входа (`OneBoxApplication`, `MainActivity`), манифест, иконки, подпись релиза |
| `:ui` | Android library | Compose-интерфейс: экраны, компоненты, тема, дизайн-кит |
| `:api` | Android library | Прослойка UI ↔ ядро: `VpnConnector`, SPI (`VpnCore`, `ProfileSource`, `VpnServiceBridge`), состояния (`VpnStatus`, `PingState`) |
| `:core-model` | JVM library | Доменные модели `Server`, `Subscription`, `SubscriptionMeta` |
| `:core-parsing` | Android library | Разбор ссылок, подписок, Base64 и JSON-конфигов |
| `:core-i18n` | JVM library | Тексты интерфейса на русском и английском |
| `:core-log` | Android library | `AppLog`: файл журнала и logcat |
| `:data` | Android library | Хранилища в `SharedPreferences`, загрузка подписок, use-case'ы, `StoreProfileSource` |
| `:engine` | Android library | `EngineBootstrap`: регистрация ядер и VpnService-моста |
| `:vpn` | Android library | Системный `VpnService` (`TunVpnService`), нейтральный к ядру |
| `:xray` (`engines/xray`) | Android library | Реализация `VpnCore` для xray: `XrayEngine`, `XrayConfigBuilder`, `GeoAssets` |
| `xray/` | Go-модуль | Обёртка над xray-core, собирается в `xray.aar` |

**Как добавить новое ядро.** Создайте модуль, реализуйте `VpnCore` и зарегистрируйте его в `EngineBootstrap.ensure` рядом с `XrayEngine`. Менять `:ui` и `:vpn` не нужно: UI работает через `VpnConnector`, а системный `VpnService` передаёт ядру `tun`-дескриптор через `VpnServiceBridge`.

## Требования

| Инструмент | Версия |
|------------|--------|
| JDK | 25 (тесты — JDK 21 через Gradle toolchain) |
| Android SDK | `platforms;android-37.0`, `build-tools;36.0.0` |
| Android NDK | `29.0.14206865` |
| Go | 1.27.1 + `gomobile` |
| Gradle | 9.6 (wrapper) |

`gomobile` устанавливается скриптом сборки ядра автоматически, если его нет.

## 🏗 Сборка из исходников

### 1. Нативное ядро (xray.aar)

`xray.aar` собирается из Go-кода и публикуется в локальный Maven-репозиторий `local-maven/`. Это делается один раз, а затем повторно — только после изменений в Go-коде:

```bash
./scripts/build-xray-aar.sh
```

### 2. Приложение

```bash
# Отладочная сборка
./gradlew assembleDebug

# Тесты и линт
./gradlew testDebugUnitTest lintDebug

# Подписанный релиз (нужен keystore.properties, см. ниже)
./gradlew assembleRelease bundleRelease
```

Готовые файлы:

- `app/build/outputs/apk/release/app-release.apk`
- `app/build/outputs/bundle/release/app-release.aab`
- `app/build/outputs/mapping/release/mapping.txt`

> **Почему `xray.aar` не лежит в `engines/xray`?** `engines/xray` — это библиотечный Android-модуль, а AGP не разрешает библиотеке подключать локальный `.aar`. Поэтому AAR публикуется в `local-maven/` и подключается как обычная зависимость `xray:xray:1.0.0@aar`. Каталог `local-maven/` не хранится в Git.

## 🔐 Подпись релиза

Параметры keystore читаются из `keystore.properties` в корне проекта:

```properties
storeFile=onebox-release.jks
storePassword=...
keyAlias=onebox
keyPassword=...
```

Сам `*.jks` / `*.keystore` и `keystore.properties` в репозиторий не добавляются. Без `keystore.properties` сборка `release` останется неподписанной.

## 📦 Релизы

Релизы собираются локально:

1. Обновите `versionName` и `versionCode` в [`app/build.gradle.kts`](app/build.gradle.kts).
2. Соберите `assembleRelease` и `bundleRelease`.
3. Скопируйте артефакты в `dist/` под именами `OneBox-<версия>.apk` и `OneBox-<версия>.aab`.
4. Сохраните `mapping.txt` как `dist/mapping-<версия>.txt` — он нужен для разбора обфусцированных крашей.
5. Снимите контрольные суммы и приложите файлы к GitHub Release:

```bash
cd dist
sha256sum OneBox-<версия>.apk OneBox-<версия>.aab \
  mapping-<версия>.txt > SHA256SUMS.txt
```

6. Поставьте тег и отправьте его в удалённый репозиторий:

```bash
git tag -a v<версия> -m "OneBox <версия>"
git push <remote> main --tags
```

### Нумерация версий

Формат версии — `X.Y.Z.W` (например, `26.0.1.1-alpha`), где `W` — номер патча поверх `X.Y.Z`. `versionCode` должен только возрастать:

| Версия | versionCode |
|--------|-------------|
| `26.0.1` | `260001` |
| `26.0.1.1` | `2600011` |
| `26.0.2` | `260002` |

Для трёхчастной версии: `X*10000 + Y*100 + Z`.
Для четырёхчастной: `(X*10000 + Y*100 + Z)*10 + W`.

## 🧪 Тесты

```bash
# Все unit-тесты
./gradlew testDebugUnitTest

# Только парсинг подписок и ссылок
./gradlew :core-parsing:testDebugUnitTest

# Только сборка конфига xray и round-trip снапшота
./gradlew :xray:testDebugUnitTest
```

Тесты `:core-parsing` и `:xray` используют Robolectric и запускаются на JDK 21, даже если сборка идёт на JDK 25.

## ❓ Частые проблемы

| Проблема | Решение |
|----------|---------|
| `Could not find xray:xray:1.0.0` | Запустите `./scripts/build-xray-aar.sh` |
| `NDK not found` | Установите NDK `29.0.14206865` через SDK Manager или задайте `ANDROID_NDK_HOME` |
| Тесты падают на Java 25 | Проверьте, что Gradle toolchain 21 установлен; модули настраивают его автоматически |
| Release без подписи | Создайте `keystore.properties` (см. раздел «Подпись релиза») |
| Подписка не импортируется | Поддерживается только `https://`; проверьте формат ссылки и доступность сервера |

## 📄 Лицензия

OneBox распространяется по лицензии [GPL-3.0](LICENSE).
Используемый xray-core также распространяется под GPL-3.0.