<div align="center">

# OneBox

**Android-клиент Xray / VLESS, VMess, Trojan, Shadowsocks — «одна коробка» для VPN-подписок**

[![CI](https://github.com/StrelaTech/onebox-dev/actions/workflows/ci.yml/badge.svg)](https://github.com/StrelaTech/onebox-dev/actions/workflows/ci.yml)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)

</div>

OneBox — Android-приложение для работы с подписками Xray и VPN-подключения через
[xray-core](https://github.com/XTLS/Xray-core). Ядро собирается из Go-кода в
`xray.aar` с помощью `gomobile bind` и работает внутри приложения поверх
`tun`-интерфейса (`VpnService`).

Поддерживаемые протоколы: **VLESS, VMess, Trojan, Shadowsocks**, а также
SOCKS/HTTP в качестве транспорта.

## Возможности

- Импорт подписок по URL (`vless://`, `vmess://`, `trojan://`, `ss://`,
 _plain-строки_, Base64) или готового JSON-конфига
- Автоматическая загрузка и обновление подписок, парсинг ссылок в серверы
- Пометки подписок: срок действия и остаток трафика
- VPN-подключение через tun-интерфейс (`TunVpnService`), остановка/запуск ядра
  без пересборки конфига
- Распаковка `geoip.dat` / `geosite.dat` при первом запуске
- Список серверов с выбором активного, SharedPreferences-хранилище
- Jetpack Compose (Material + [Miuix](https://github.com/miuix-kotlin/miuix))

## Структура проекта

| Модуль    | Тип              | Назначение                                                        |
|-----------|------------------|-------------------------------------------------------------------|
| `app`     | application      | Точка входа (`MainActivity`), иконки, манифест, подпись релиза     |
| `ui`      | library          | Compose-интерфейс: экраны, компоненты, тема, сгенерированные иконки |
| `core`    | library          | Чистая логика: парсинг ссылок/подписок, сборка JSON-конфига ядра, i18n-строки |
| `data`    | library          | Хранилища (SharedPreferences), загрузка подписок (OkHttp), use-case'ы |
| `engine`  | library          | `TunVpnService`, обёртка `XrayController`, управление гео-файлами   |
| `xray`    | Go-модуль        | Обёртка над xray-core, собирается в `xray.aar` скриптом внизу       |

Зависимости модулей: `app → ui → engine → data → core`; `engine` подключает
нативный `xray.aar`.

## Требования

- **JDK 25** — сборка. Unit-тесты `core` автоматически используют JDK 21 через
  Gradle toolchain (Robolectric 4.17 не работает на Java 25)
- **Android SDK**: `platforms;android-37.0`, `build-tools;36.0.0`
- **Android NDK**: `29.0.14206865`
- **Go**: `1.27.1`, `gomobile` (устанавливается скриптом при необходимости)

## Сборка

```bash
# 1. Собрать xray.aar и опубликовать в локальный Maven-репозиторий
./scripts/build-xray-aar.sh

# 2. Отладочный APK
./gradlew assembleDebug

# 3. Тесты и линт
./gradlew testDebugUnitTest lintDebug

# 4. Подписанный релиз (нужен keystore.properties, см. ниже)
./gradlew assembleRelease bundleRelease
```

Артефакты: `app/build/outputs/apk/release/*.apk`,
`app/build/outputs/bundle/release/*.aab`.

> **Почему `xray.aar` не кладётся в модуль `engine` напрямую?** `engine` —
> библиотечный модуль, а AGP не разрешает библиотеке подключать локальный
> `.aar`. Поэтому AAR публикуется в локальный Maven-репозиторий
> `local-maven/` через `scripts/build-xray-aar.sh` и подключается как
> обычная зависимость `xray:xray:1.0.0@aar`. Каталог `local-maven/` в git
> не хранится.

## Подпись

Релиз подписывается keystore-файлом, данные передаются через
`keystore.properties` в корне (файл не входит в git):

```properties
storeFile=onebox-release.jks
storePassword=...
keyAlias=onebox
keyPassword=...
```

Сам `*.jks` / `*.keystore` в репозиторий не коммится. Без
`keystore.properties` сборка `release` пойдёт без подписи (unsigned).

## Версионирование

`versionName` / `versionCode` задаются в [`app/build.gradle.kts`](app/build.gradle.kts)
и автоматически обновляются release-workflow. Версия `X.Y.Z` соответствует
`versionCode = X*10000 + Y*100 + Z` (например, `26.0.1-alpha` → `260001`).

## CI / CD

- **CI** (`.github/workflows/ci.yml`) — на каждый push / PR в `main`:
  собирает `xray.aar` → `assembleDebug` → unit-тесты → `lintDebug`,
  выкладывает артефакты (debug APK, отчёты тестов и линта).
- **Release** (`.github/workflows/release.yml`) — ручной запуск
  (`workflow_dispatch`): собирает подписанные APK и AAB, проверяет подпись
  `apksigner`, бампит версию, создаёт GitHub Release с тегом `vX.Y.Z`, после
  сборки удаляет файлы подписи.

Для релиза нужны секреты репозитория:

| Секрет              | Описание                                  |
|---------------------|-------------------------------------------|
| `KEYSTORE_BASE64`   | вывод `base64 -w0 onebox-release.jks`     |
| `KEYSTORE_PASSWORD` | пароль keystore                           |
| `KEY_ALIAS`         | псевдоним ключа (например, `onebox`)      |
| `KEY_PASSWORD`      | пароль ключа                              |

## Лицензия

Распространяется по [GPL-3.0](LICENSE).
