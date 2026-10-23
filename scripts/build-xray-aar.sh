#!/usr/bin/env bash
# Сборка xray-core в AAR для Android (gomobile bind) и копирование в app/libs.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
XRAY_DIR="$ROOT/xray"
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
mkdir -p "$XRAY_DIR/build"
gomobile bind \
    -target=android \
    -androidapi 24 \
    -trimpath \
    "-ldflags=-s -w" \
    -o "$XRAY_DIR/build/xray.aar" \
    .

cp "$XRAY_DIR/build/xray.aar" "$ROOT/app/libs/xray.aar"
echo "Готово: app/libs/xray.aar"
