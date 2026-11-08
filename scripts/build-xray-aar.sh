#!/usr/bin/env bash
# Сборка xray-core в AAR для Android (gomobile bind).
# Артефакт публикуется в локальный Maven-репозиторий local-maven/, откуда его
# подхватывает библиотечный модуль engines/xray (локальные .aar в AAR-модулях
# AGP не поддерживает).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
XRAY_DIR="$ROOT/xray"
REPO_DIR="$ROOT/local-maven"
GROUP="xray"
ARTIFACT="xray"
VERSION="1.0.0"
DEST_DIR="$REPO_DIR/$GROUP/$ARTIFACT/$VERSION"

export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
# javac может отсутствовать в системе (JRE) — добавляем Temurin JDK если установлен
JDK_DIR="$(ls -d "$HOME"/.local/opt/jdk-21* 2>/dev/null | head -1)"
[ -n "$JDK_DIR" ] && export PATH="$JDK_DIR/bin:$PATH"
export PATH="$HOME/go/bin:$PATH"

if ! command -v javac >/dev/null 2>&1; then
    echo "javac не найден. Убедитесь, что JAVA_HOME указывает на JDK (не JRE)." >&2
    exit 1
fi

# Найти установленный NDK (или поставить через sdkmanager, если не установлен)
if [ -z "${ANDROID_NDK_HOME:-}" ]; then
    NDK_DIR=""
    for sdk in "$ANDROID_HOME" "${ANDROID_SDK_ROOT:-}" "$HOME/Android/Sdk" "/usr/local/lib/android/sdk"; do
        [ -z "$sdk" ] && continue
        if [ -d "$sdk/ndk" ]; then
            NDK_DIR="$(ls -d "$sdk/ndk"/* 2>/dev/null | sort -V | tail -1)"
            [ -n "$NDK_DIR" ] && break
        fi
        if [ -d "$sdk/ndk-bundle" ]; then
            NDK_DIR="$sdk/ndk-bundle"
            break
        fi
    done
    if [ -z "$NDK_DIR" ] && [ -n "${ANDROID_HOME:-}" ] && [ -x "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]; then
        echo "NDK не найден, устанавливаю ndk;29.0.14206865 через sdkmanager..."
        yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --install "ndk;29.0.14206865" >/dev/null 2>&1 || true
        NDK_DIR="$(ls -d "$ANDROID_HOME/ndk"/* 2>/dev/null | sort -V | tail -1)"
        [ -z "$NDK_DIR" ] && NDK_DIR="$ANDROID_HOME/ndk-bundle"
    fi
    [ -n "$NDK_DIR" ] || { echo "NDK не найден. Проверьте ANDROID_HOME=$ANDROID_HOME, ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT"; exit 1; }
    export ANDROID_NDK_HOME="$NDK_DIR"
fi
echo "Using NDK: $ANDROID_NDK_HOME"

# Убедиться, что платформа android-$ANDROID_API установлена (требуется gomobile bind)
ANDROID_API="${ANDROID_API:-24}"
if [ -d "${ANDROID_HOME}/platforms" ] && [ ! -d "${ANDROID_HOME}/platforms/android-${ANDROID_API}" ]; then
    if [ -x "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]; then
        echo "Platform android-${ANDROID_API} не найдена, устанавливаю platforms;android-${ANDROID_API} через sdkmanager..."
        yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --install "platforms;android-${ANDROID_API}" >/dev/null 2>&1 || true
    fi
fi
echo "Using ANDROID_API=$ANDROID_API"

cd "$XRAY_DIR"

# gomobile ставим, если его нет: версию берём из go.mod (golang.org/x/mobile)
if ! command -v gomobile >/dev/null 2>&1; then
    MOBILE_VERSION="$(go list -m -f '{{.Version}}' golang.org/x/mobile)"
    echo "Устанавливаю gomobile $MOBILE_VERSION"
    go install "golang.org/x/mobile/cmd/gomobile@$MOBILE_VERSION"
    export PATH="$(go env GOBIN):$PATH"
fi
command -v gomobile >/dev/null 2>&1 || export PATH="$(go env GOPATH)/bin:$PATH"
echo "Using gomobile: $(command -v gomobile)"
gomobile init

mkdir -p "$XRAY_DIR/build"
gomobile bind \
    -target=android \
    -androidapi "$ANDROID_API" \
    -trimpath \
    "-ldflags=-s -w" \
    -o "$XRAY_DIR/build/xray.aar" \
    .

# Публикация в локальный Maven-репозиторий
mkdir -p "$DEST_DIR"
cp "$XRAY_DIR/build/xray.aar" "$DEST_DIR/$ARTIFACT-$VERSION.aar"
cat > "$DEST_DIR/$ARTIFACT-$VERSION.pom" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <groupId>$GROUP</groupId>
    <artifactId>$ARTIFACT</artifactId>
    <version>$VERSION</version>
    <packaging>aar</packaging>
    <name>$ARTIFACT</name>
    <description>xray-core built with gomobile bind (Android)</description>
</project>
EOF

echo "Готово: $DEST_DIR/$ARTIFACT-$VERSION.aar"
