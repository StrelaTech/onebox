#!/usr/bin/env bash
# Сборка xray-core в AAR для Android (gomobile bind).
# Артефакт публикуется в локальный Maven-репозиторий local-maven/, откуда его
# подхватывает библиотечный модуль engine (локальные .aar в AAR-модулях
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

# Найти установленный NDK
if [ -z "${ANDROID_NDK_HOME:-}" ]; then
    NDK_DIR="$(ls -d "$ANDROID_HOME"/ndk/* 2>/dev/null | sort -V | tail -1)"
    [ -n "$NDK_DIR" ] || { echo "NDK не найден в $ANDROID_HOME/ndk"; exit 1; }
    export ANDROID_NDK_HOME="$NDK_DIR"
fi
echo "Using NDK: $ANDROID_NDK_HOME"

cd "$XRAY_DIR"

# gomobile ставим, если его нет: версию берём из go.mod (golang.org/x/mobile)
if ! command -v gomobile >/dev/null 2>&1; then
    MOBILE_VERSION="$(go list -m -f '{{.Version}}' golang.org/x/mobile)"
    echo "Устанавливаю gomobile $MOBILE_VERSION"
    go install "golang.org/x/mobile/cmd/gomobile@$MOBILE_VERSION"
    export PATH="$(go env GOBIN):$PATH"
fi
command -v gomobile >/dev/null 2>&1 || export PATH="$(go env GOPATH)/bin:$PATH"
gomobile init

mkdir -p "$XRAY_DIR/build"
gomobile bind \
    -target=android \
    -androidapi 24 \
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
