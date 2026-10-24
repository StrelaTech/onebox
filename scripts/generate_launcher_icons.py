#!/usr/bin/env python3
"""Генерирует иконки приложения из scripts/ic_launcher.svg.

Источник (scripts/ic_launcher.svg) используется двояко:
  * адаптивная иконка (API 26+) — векторы
    app/src/main/res/drawable/ic_launcher_background.xml (фон)
    app/src/main/res/drawable/ic_launcher_foreground.xml (макет в безопасной
    зоне круга 66dp холста 108dp), monochrome-слой ссылается на foreground;
  * legacy-иконки (API 24–25) — webp во всех плотностях, квадратная
    (ic_launcher.webp) и круглая с прозрачными углами (ic_launcher_round.webp).

Запуск: python3 scripts/generate_launcher_icons.py
Нужен rsvg-convert (librsvg) и magick (ImageMagick).
"""
from __future__ import annotations

import shutil
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SVG = ROOT / "scripts/ic_launcher.svg"
MIPMAP = ROOT / "app/src/main/res"

# Плотность: размер стороны webp в px (базовый размер иконки — 48dp).
DENSITIES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}


def run(*args: str) -> None:
    subprocess.run(args, check=True)


def main() -> None:
    for tool in ("rsvg-convert", "magick"):
        if shutil.which(tool) is None:
            raise SystemExit(f"Не найден {tool} — установите librsvg и ImageMagick")

    with tempfile.TemporaryDirectory() as tmp:
        tmp_dir = Path(tmp)
        for folder, size in DENSITIES.items():
            dest = MIPMAP / folder
            if not dest.is_dir():
                raise SystemExit(f"Нет каталога {dest.relative_to(ROOT)}")

            png = tmp_dir / f"icon_{size}.png"
            run(
                "rsvg-convert",
                "--width", str(size),
                "--height", str(size),
                "--output", str(png),
                str(SVG),
            )

            # Квадратная иконка — полный макет.
            run("magick", str(png), str(dest / "ic_launcher.webp"))

            # Круглая — обрезка по кругу, углы прозрачные.
            run(
                "magick", str(png),
                "(", "+clone", "-alpha", "extract",
                "-draw", f"circle {size / 2},{size / 2} {size / 2},0", ")",
                "-compose", "CopyOpacity", "-composite",
                str(dest / "ic_launcher_round.webp"),
            )

            print(f"{folder}: {size}x{size}")


if __name__ == "__main__":
    main()
