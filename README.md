# OneBox

Android-клиент для работы с подписками Xray (VLESS/VMess/Trojan и др.) и
VPN-подключения через [xray-core](https://github.com/XTLS/Xray-core),
собранный в AAR через `gomobile bind`.

## Структура проекта

| Модуль    | Назначение                                                        |
|-----------|-------------------------------------------------------------------|
| `app`     | Точка входа (`MainActivity`), сборка APK/AAB, подпись             |
| `ui`      | Compose-интерфейс (экраны, компоненты, тема, иконки)              |
| `core`    | Чистая логика: парсинг ссылок/подписок, сборка JSON-конфига ядра  |
| `data`    | Хранилища (SharedPreferences), загрузка подписок, use-case'ы      |
| `engine`  | VPN-сервис и обёртка над нативным `xray.aar`                      |
| `xray`    | Go-обёртка над xray-core (собирается в AAR)                       |

## Требования

- JDK 25
- Android SDK: `platforms;android-37.0`, `build-tools;36.0.0`
- Android NDK: `29.0.14206865`
- Go 1.27.1
- `gomobile` (устанавливается скриптом при необходимости)

## Сборка

```bash
# 1. Собрать xray.aar и опубликовать в local-maven/
./scripts/build-xray-aar.sh

# 2. Собрать приложение
./gradlew assembleDebug
```

> `engine` — библиотечный модуль, поэтому он не может зависеть от локального
> `.aar` напрямую. AAR публикуется в `local-maven/` и подключается как обычная
> Maven-зависимость (`xray:xray:1.0.0@aar`). Каталог `local-maven/` в git не
> хранится — его создаёт `scripts/build-xray-aar.sh`.

## Тесты и проверки

```bash
./gradlew testDebugUnitTest   # unit-тесты модулей
./gradlew lintDebug           # Android Lint
./gradlew assembleRelease     # release APK (при наличии keystore)
./gradlew bundleRelease       # release AAB
```

## Подпись релиза

Создайте `keystore.properties` в корне (файл в `.gitignore`):

```properties
storeFile=onebox-release.jks
storePassword=...
keyAlias=onebox
keyPassword=...
```

keystore (`*.jks`, `*.keystore`) в репозиторий не добавляется.

## CI/CD

- **CI** (`.github/workflows/ci.yml`) — на каждый push/PR в `main`: собирает
  `xray.aar`, `assembleDebug`, unit-тесты и `lintDebug`, публикует артефакты.
- **Release** (`.github/workflows/release.yml`) — запускается вручную
  (`workflow_dispatch`): собирает подписанные APK и AAB, создаёт GitHub Release.

Для релиза нужны секреты репозитория:

| Секрет              | Описание                                  |
|---------------------|-------------------------------------------|
| `KEYSTORE_BASE64`   | `base64 -w0 onebox-release.jks`           |
| `KEYSTORE_PASSWORD` | Пароль keystore                           |
| `KEY_ALIAS`         | Псевдоним ключа (например, `onebox`)      |
| `KEY_PASSWORD`      | Пароль ключа                              |

## Лицензия

См. [LICENSE](LICENSE).
