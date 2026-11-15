#!/usr/bin/env python3
"""Синхронизирует SVG-иконки StrelaTech и генерирует Compose-объект OneBoxIcons.

Источник: https://github.com/StrelaTech/icons (outline/ и filled/).
SVG-исходники складываются в app/src/main/res/icons/, а из них собирается
kit/src/main/java/com/strela/onebox/ui/design/icons/OneBoxIcons.kt.

Запуск:
    scripts/import-icons.py                 # клонирует репозиторий во временную папку
    scripts/import-icons.py --source DIR    # использует уже скачанный репозиторий
"""

from __future__ import annotations

import argparse
import math
import re
import shutil
import subprocess
import tempfile
import xml.etree.ElementTree as ET
from pathlib import Path

REPO_URL = "https://github.com/StrelaTech/icons"
ROOT = Path(__file__).resolve().parent.parent
SVG_DIR = ROOT / "app" / "src" / "main" / "res" / "icons"
# Лицензия исходного набора иконок не является Android-ресурсом, поэтому
# держим её вне res/ (иначе падает :app:packageDebugResources).
LICENSE_FILE = ROOT / "app" / "src" / "main" / "icons" / "LICENSE"
OUTPUT = (
    ROOT
    / "kit"
    / "src"
    / "main"
    / "java"
    / "com"
    / "strela"
    / "onebox"
    / "ui"
    / "design"
    / "icons"
    / "OneBoxIcons.kt"
)

# Имена, которые уже используются в UI и должны остаться совместимыми.
OUTLINE_OVERRIDES = {
    "home": "HomeOutline",
    "settings": "SettingsOutline",
    "globe": "Globe",
    "onebox-logo": "OneBoxLogo",
}
FILLED_OVERRIDES = {
    "home": "Home",
    "settings": "Settings",
    "globe": "GlobeFilled",
    "onebox-logo": "OneBoxLogoFilled",
}
# Иконок из прошлого набора нет в StrelaTech — оставляем псевдонимы.
COMPAT_ALIASES = {
    "Clipboard": "File",
    "FormatSize": "Resize",
    "Layers": "Database",
}

STYLE_ATTRS = (
    "fill",
    "stroke",
    "stroke-width",
    "stroke-linecap",
    "stroke-linejoin",
    "fill-rule",
)


def local_name(tag: str) -> str:
    return tag.rsplit("}", 1)[-1]


def num(value: str | None, default: float = 0.0) -> float:
    if value is None:
        return default
    match = re.search(r"-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?", value)
    return float(match.group(0)) if match else default


def fmt(value: float) -> str:
    if abs(value - round(value)) < 1e-9:
        return str(int(round(value)))
    return f"{value:.4f}".rstrip("0").rstrip(".")


def is_on(value: str | None) -> bool:
    return value is not None and value.strip().lower() not in ("none", "transparent")


def pascal(name: str) -> str:
    return "".join(part[:1].upper() + part[1:] for part in re.split(r"[-_]", name))


def style_of(element: ET.Element, inherited: dict[str, str]) -> dict[str, str]:
    style = dict(inherited)
    for attr in STYLE_ATTRS:
        value = element.get(attr)
        if value is not None:
            style[attr] = value
    return style


def point_pairs(points: str) -> list[tuple[float, float]]:
    values = [num(v) for v in re.findall(r"-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?", points)]
    return list(zip(values[0::2], values[1::2]))


def circle_path(cx: float, cy: float, r: float) -> str:
    return (
        f"M{fmt(cx - r)} {fmt(cy)}"
        f"a{fmt(r)} {fmt(r)} 0 1 0 {fmt(2 * r)} 0"
        f"a{fmt(r)} {fmt(r)} 0 1 0 {fmt(-2 * r)} 0Z"
    )


def ellipse_path(cx: float, cy: float, rx: float, ry: float) -> str:
    return (
        f"M{fmt(cx - rx)} {fmt(cy)}"
        f"a{fmt(rx)} {fmt(ry)} 0 1 0 {fmt(2 * rx)} 0"
        f"a{fmt(rx)} {fmt(ry)} 0 1 0 {fmt(-2 * rx)} 0Z"
    )


def rect_path(x: float, y: float, w: float, h: float, rx: float, ry: float) -> str:
    rx = min(max(rx, 0.0), w / 2)
    ry = min(max(ry, 0.0), h / 2)
    if rx <= 0 or ry <= 0:
        return f"M{fmt(x)} {fmt(y)}h{fmt(w)}v{fmt(h)}h{fmt(-w)}Z"
    return (
        f"M{fmt(x + rx)} {fmt(y)}"
        f"h{fmt(w - 2 * rx)}"
        f"a{fmt(rx)} {fmt(ry)} 0 0 1 {fmt(rx)} {fmt(ry)}"
        f"v{fmt(h - 2 * ry)}"
        f"a{fmt(rx)} {fmt(ry)} 0 0 1 {fmt(-rx)} {fmt(ry)}"
        f"h{fmt(-(w - 2 * rx))}"
        f"a{fmt(rx)} {fmt(ry)} 0 0 1 {fmt(-rx)} {fmt(-ry)}"
        f"v{fmt(-(h - 2 * ry))}"
        f"a{fmt(rx)} {fmt(ry)} 0 0 1 {fmt(rx)} {fmt(-ry)}Z"
    )


def shape_path(element: ET.Element) -> str | None:
    tag = local_name(element.tag)
    if tag == "path":
        data = element.get("d")
        return re.sub(r"\s+", " ", data).strip() if data else None
    if tag == "line":
        x1, y1 = num(element.get("x1")), num(element.get("y1"))
        x2, y2 = num(element.get("x2")), num(element.get("y2"))
        return f"M{fmt(x1)} {fmt(y1)}L{fmt(x2)} {fmt(y2)}"
    if tag == "circle":
        return circle_path(num(element.get("cx")), num(element.get("cy")), num(element.get("r")))
    if tag == "ellipse":
        return ellipse_path(
            num(element.get("cx")),
            num(element.get("cy")),
            num(element.get("rx")),
            num(element.get("ry")),
        )
    if tag == "rect":
        rx = num(element.get("rx"), 0.0)
        ry = num(element.get("ry"), rx)
        return rect_path(
            num(element.get("x")),
            num(element.get("y")),
            num(element.get("width")),
            num(element.get("height")),
            rx,
            ry,
        )
    if tag == "polyline":
        points = point_pairs(element.get("points", ""))
        if not points:
            return None
        head, *tail = points
        return f"M{fmt(head[0])} {fmt(head[1])}" + "".join(
            f"L{fmt(x)} {fmt(y)}" for x, y in tail
        )
    if tag == "polygon":
        points = point_pairs(element.get("points", ""))
        if not points:
            return None
        head, *tail = points
        return f"M{fmt(head[0])} {fmt(head[1])}" + "".join(
            f"L{fmt(x)} {fmt(y)}" for x, y in tail
        ) + "Z"
    return None


def collect_paths(
    element: ET.Element, inherited: dict[str, str], result: list[dict]
) -> None:
    tag = local_name(element.tag)
    if tag in ("defs", "clipPath", "mask"):
        return
    style = style_of(element, inherited)
    path = shape_path(element)
    if path:
        result.append(
            {
                "d": path,
                "fill": is_on(style.get("fill")),
                "stroke": is_on(style.get("stroke")),
                "strokeWidth": num(style.get("stroke-width"), 2.5),
                "evenOdd": style.get("fill-rule", "").lower() == "evenodd",
            }
        )
        return
    for child in element:
        collect_paths(child, style, result)


def parse_svg(path: Path) -> tuple[str, str, list[dict]]:
    root = ET.parse(path).getroot()
    view_box = root.get("viewBox", "0 0 24 24").split()
    width = num(view_box[2], 24.0) if len(view_box) >= 4 else 24.0
    height = num(view_box[3], 24.0) if len(view_box) >= 4 else 24.0
    paths: list[dict] = []
    collect_paths(root, {}, paths)
    return fmt(width), fmt(height), paths


def kotlin_string(value: str) -> str:
    return '"' + value.replace("\\", "\\\\").replace('"', '\\"') + '"'


def render_icon(property_name: str, source: Path, note: str = "") -> str:
    width, height, paths = parse_svg(source)
    comment = f"    /** StrelaTech: `{source.parent.name}/{source.name}`{note}. */"
    lines = [
        comment,
        f"    val {property_name}: ImageVector by lazy {{",
        f"        icon(\"{property_name}\", viewport = {width}f) {{",
    ]
    if width != height:
        raise ValueError(f"{source}: не поддерживается неквадратный viewBox ({width}x{height})")
    for item in paths:
        args = [kotlin_string(item["d"])]
        if item["fill"]:
            args.append("fill = true")
        if item["stroke"]:
            args.append("stroke = true")
            if math.isclose(item["strokeWidth"], 2.5):
                pass
            else:
                args.append(f"strokeWidth = {fmt(item['strokeWidth'])}f")
        if item["evenOdd"]:
            args.append("evenOdd = true")
        lines.append("            path(" + ", ".join(args) + ")")
    lines += ["        }", "    }", ""]
    return "\n".join(lines)


def sync_sources(source: Path) -> None:
    SVG_DIR.mkdir(parents=True, exist_ok=True)
    for variant in ("outline", "filled"):
        src = source / variant
        if not src.is_dir():
            raise SystemExit(f"Нет папки {src}")
        dst = SVG_DIR / variant
        if dst.exists():
            shutil.rmtree(dst)
        shutil.copytree(src, dst)
        # Убираем хвостовые пробелы, чтобы diff не ругался на файлы из апстрима.
        for svg in dst.glob("*.svg"):
            lines = [line.rstrip() for line in svg.read_text(encoding="utf-8").splitlines()]
            svg.write_text("\n".join(lines) + "\n", encoding="utf-8")
    license_file = source / "LICENSE"
    if license_file.exists():
        LICENSE_FILE.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(license_file, LICENSE_FILE)


def generate() -> None:
    outline_names = sorted(p.stem for p in (SVG_DIR / "outline").glob("*.svg"))
    filled_names = sorted(p.stem for p in (SVG_DIR / "filled").glob("*.svg"))

    body: list[str] = []
    for name in outline_names:
        prop = OUTLINE_OVERRIDES.get(name, pascal(name))
        body.append(render_icon(prop, SVG_DIR / "outline" / f"{name}.svg"))
    for name in filled_names:
        prop = FILLED_OVERRIDES.get(name, pascal(name) + "Filled")
        source = SVG_DIR / "filled" / f"{name}.svg"
        note = ""
        # Compose не умеет SVG-маски; для таких иконок берём outline-вариант.
        if "mask=" in source.read_text(encoding="utf-8"):
            outline = SVG_DIR / "outline" / f"{name}.svg"
            if outline.exists():
                source, note = outline, " (маска заменена outline-вариантом)"
        body.append(render_icon(prop, source, note))

    aliases = "\n".join(
        f"    val {alias}: ImageVector get() = {target}" for alias, target in COMPAT_ALIASES.items()
    )

    text = f'''package com.strela.onebox.ui.design.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Иконки OneBox из набора [StrelaTech Icons](https://github.com/StrelaTech/icons).
 *
 * Сетка 24x24, обводка 2.5 px со скруглёнными концами и стыками. Исходные SVG
 * лежат в `app/src/main/res/icons/{{outline,filled}}`, файл сгенерирован
 * `scripts/import-icons.py` — правьте SVG и перегенерируйте, а не этот файл.
 *
 * Для нижней навигации пары: outline-варианты ([HomeOutline], [Globe],
 * [SettingsOutline]) рисуются для невыбранной вкладки, filled-варианты
 * ([Home], [GlobeFilled], [Settings]) — для выбранной.
 * Цвет задаётся при отрисовке через `Icon(tint = ...)`.
 */
object OneBoxIcons {{

{chr(10).join(body)}
    // Псевдонимы для имён из прежнего набора Material Symbols.
{aliases}
}}

private fun icon(
    name: String,
    viewport: Float,
    build: ImageVector.Builder.() -> Unit,
): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = viewport,
        viewportHeight = viewport,
    ).apply(build).build()

private fun ImageVector.Builder.path(
    d: String,
    fill: Boolean = false,
    stroke: Boolean = false,
    strokeWidth: Float = 2.5f,
    evenOdd: Boolean = false,
) {{
    addPath(
        pathData = PathParser().parsePathString(d).toNodes(),
        pathFillType = if (evenOdd) PathFillType.EvenOdd else PathFillType.NonZero,
        fill = if (fill) SolidColor(Color.Black) else null,
        stroke = if (stroke) SolidColor(Color.Black) else null,
        strokeLineWidth = strokeWidth,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    )
}}
'''
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(text, encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, help="путь к уже скачанному StrelaTech/icons")
    args = parser.parse_args()

    if args.source:
        source = args.source.resolve()
        sync_sources(source)
    else:
        with tempfile.TemporaryDirectory(prefix="strela-icons-") as tmp:
            clone = Path(tmp) / "icons"
            subprocess.run(
                ["git", "clone", "--depth", "1", REPO_URL, str(clone)],
                check=True,
            )
            sync_sources(clone)

    generate()
    print(f"Иконки: {SVG_DIR}")
    print(f"Сгенерировано: {OUTPUT}")


if __name__ == "__main__":
    main()
