#!/usr/bin/env python3
"""Генерирует Compose-иконки (ImageVector) из SVG в app/src/main/res/icons.

Для каждой иконки берётся filled-вариант, а если его нет (или он использует
неподдерживаемую маску) — outline. Скрипт поддерживает path/circle/line/rect/
polyline/polygon/ellipse, наследование fill/stroke/stroke-width и fill-rule.
Запуск: python3 scripts/generate_icons.py
"""
from __future__ import annotations

import re
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC_OUTLINE = ROOT / "app/src/main/res/icons/outline"
SRC_FILLED = ROOT / "app/src/main/res/icons/filled"
DST = ROOT / "app/src/main/java/com/strela/onebox/ui/design/icons/OneBoxIcons.kt"

SKIP_TAGS = {"defs", "clipPath", "mask", "linearGradient", "radialGradient", "style"}
# Маски задают прозрачные вырезы, которые ImageVector не умеет повторять.
UNSUPPORTED_TAGS = {"mask"}
STROKE_TAGS = {"path", "line", "rect", "circle", "ellipse", "polyline", "polygon"}


def num(value: str | None, default: float = 0.0) -> float:
    if value is None:
        return default
    return float(value.rstrip("px"))


def fmt(value: float) -> str:
    text = f"{value:.4f}".rstrip("0").rstrip(".")
    return text if text not in {"", "-0"} else "0"


def camel(name: str) -> str:
    parts = re.split(r"[^A-Za-z0-9]+", name)
    out = ""
    for part in parts:
        if not part:
            continue
        if part.isupper() and len(part) <= 3:
            out += part
        else:
            out += part[:1].upper() + part[1:]
    return out


def color_value(value: str | None) -> bool:
    """True, если элемент залит/обведён (цвет всё равно заменится tint'ом)."""
    return value is not None and value.lower() not in {"none", "transparent"}


def rect_path(x: float, y: float, w: float, h: float, rx: float, ry: float) -> str:
    if rx <= 0 and ry <= 0:
        return f"M{fmt(x)},{fmt(y)}h{fmt(w)}v{fmt(h)}h{fmt(-w)}Z"
    rx = min(rx, w / 2)
    ry = min(ry or rx, h / 2)
    return (
        f"M{fmt(x + rx)},{fmt(y)}"
        f"h{fmt(w - 2 * rx)}"
        f"a{fmt(rx)},{fmt(ry)} 0 0 1 {fmt(rx)},{fmt(ry)}"
        f"v{fmt(h - 2 * ry)}"
        f"a{fmt(rx)},{fmt(ry)} 0 0 1 {fmt(-rx)},{fmt(ry)}"
        f"h{fmt(-(w - 2 * rx))}"
        f"a{fmt(rx)},{fmt(ry)} 0 0 1 {fmt(-rx)},{fmt(-ry)}"
        f"v{fmt(-(h - 2 * ry))}"
        f"a{fmt(rx)},{fmt(ry)} 0 0 1 {fmt(rx)},{fmt(-ry)}Z"
    )


def element_path(tag: str, el: ET.Element) -> str | None:
    if tag == "path":
        return el.get("d")
    if tag == "line":
        return f"M{el.get('x1', '0')},{el.get('y1', '0')}L{el.get('x2', '0')},{el.get('y2', '0')}"
    if tag == "rect":
        x, y = num(el.get("x")), num(el.get("y"))
        w, h = num(el.get("width")), num(el.get("height"))
        rx = num(el.get("rx"))
        ry = num(el.get("ry"), rx)
        return rect_path(x, y, w, h, rx, ry)
    if tag == "circle":
        cx, cy, r = num(el.get("cx")), num(el.get("cy")), num(el.get("r"))
        return (
            f"M{fmt(cx - r)},{fmt(cy)}"
            f"a{fmt(r)},{fmt(r)} 0 1 0 {fmt(2 * r)},0"
            f"a{fmt(r)},{fmt(r)} 0 1 0 {fmt(-2 * r)},0Z"
        )
    if tag == "ellipse":
        cx, cy = num(el.get("cx")), num(el.get("cy"))
        rx, ry = num(el.get("rx")), num(el.get("ry"))
        return (
            f"M{fmt(cx - rx)},{fmt(cy)}"
            f"a{fmt(rx)},{fmt(ry)} 0 1 0 {fmt(2 * rx)},0"
            f"a{fmt(rx)},{fmt(ry)} 0 1 0 {fmt(-2 * rx)},0Z"
        )
    if tag in {"polyline", "polygon"}:
        tokens = [p for p in re.split(r"[\s,]+", el.get("points", "").strip()) if p]
        if len(tokens) < 2:
            return None
        parts = []
        for i in range(0, len(tokens) - 1, 2):
            parts.append(("M" if i == 0 else "L") + f"{tokens[i]},{tokens[i + 1]}")
        if tag == "polygon":
            parts.append("Z")
        return "".join(parts)
    return None


def collect(el: ET.Element, style: dict, out: list[dict]) -> None:
    tag = el.tag.split("}")[-1]
    if tag in SKIP_TAGS:
        return

    inherited = style.copy()
    for key in ("fill", "stroke", "stroke-width", "stroke-linecap", "stroke-linejoin", "fill-rule"):
        if key in el.attrib:
            inherited[key] = el.attrib[key]

    if tag in STROKE_TAGS:
        d = element_path(tag, el)
        if d:
            out.append(
                {
                    "d": d,
                    "fill": color_value(inherited.get("fill", "none")),
                    "stroke": color_value(inherited.get("stroke")),
                    "width": num(inherited.get("stroke-width"), 2.5),
                    "cap": inherited.get("stroke-linecap", "butt"),
                    "join": inherited.get("stroke-linejoin", "miter"),
                    "evenOdd": inherited.get("fill-rule") == "evenodd",
                }
            )

    for child in el:
        collect(child, inherited, out)


def capitalize_cap(value: str) -> str:
    return {"round": "StrokeCap.Round", "square": "StrokeCap.Square"}.get(value, "StrokeCap.Butt")


def capitalize_join(value: str) -> str:
    return {"round": "StrokeJoin.Round", "bevel": "StrokeJoin.Bevel"}.get(value, "StrokeJoin.Miter")


def build_icon(path: Path) -> str:
    root = ET.parse(path).getroot()
    style = {
        "fill": root.get("fill", "none"),
        "stroke": root.get("stroke"),
        "stroke-width": root.get("stroke-width", "2.5"),
        "stroke-linecap": root.get("stroke-linecap", "butt"),
        "stroke-linejoin": root.get("stroke-linejoin", "miter"),
        "fill-rule": root.get("fill-rule"),
    }
    shapes: list[dict] = []
    collect(root, style, shapes)

    lines = []
    for shape in shapes:
        args = [f'"{shape["d"]}"']
        if shape["fill"]:
            args.append("fill = true")
        if shape["stroke"]:
            args.append("stroke = true")
            if shape["width"] != 2.5:
                args.append(f'width = {fmt(shape["width"])}f')
            if shape["cap"] != "butt":
                args.append(f"cap = {capitalize_cap(shape['cap'])}")
            if shape["join"] != "miter":
                args.append(f"join = {capitalize_join(shape['join'])}")
        if shape["evenOdd"]:
            args.append("evenOdd = true")
        lines.append(f"            svgPath({', '.join(args)})")

    # Некоторые иконки состоят только из заливки — stroke по умолчанию не нужен.
    body = "\n".join(lines)
    return f'    val {camel(path.stem)}: ImageVector by lazy {{\n        image("{camel(path.stem)}") {{\n{body}\n        }}\n    }}\n'


HEADER = '''package com.strela.onebox.ui.design.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Иконки OneBox. Сгенерировано из `res/icons` скриптом
 * `scripts/generate_icons.py` — не редактировать вручную.
 *
 * Предпочтение отдаётся filled-варианту, outline используется как запасной
 * (например, для иконок, которых нет в filled, или если в SVG есть маска).
 * Цвет задаётся при отрисовке через `Icon(tint = ...)`, поэтому в самих
 * векторах используется чёрный.
 */
object OneBoxIcons {
'''

HELPERS = '''}

private fun image(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply(block).build()

private fun ImageVector.Builder.svgPath(
    d: String,
    fill: Boolean = false,
    stroke: Boolean = false,
    width: Float = 2.5f,
    cap: StrokeCap = StrokeCap.Round,
    join: StrokeJoin = StrokeJoin.Round,
    evenOdd: Boolean = false
) {
    addPath(
        pathData = PathParser().parsePathString(d).toNodes(),
        pathFillType = if (evenOdd) PathFillType.EvenOdd else PathFillType.NonZero,
        fill = if (fill) SolidColor(Color.Black) else null,
        stroke = if (stroke) SolidColor(Color.Black) else null,
        strokeLineWidth = if (stroke) width else 0f,
        strokeLineCap = cap,
        strokeLineJoin = join
    )
}
'''


def has_unsupported_svg(path: Path) -> bool:
    return any(
        el.tag.split("}")[-1] in UNSUPPORTED_TAGS
        for el in ET.parse(path).getroot().iter()
    )


def pick_source(outline: Path) -> Path:
    filled = SRC_FILLED / outline.name
    if filled.exists() and not has_unsupported_svg(filled):
        return filled
    return outline


def main() -> None:
    icons = sorted(
        (pick_source(icon) for icon in SRC_OUTLINE.glob("*.svg")),
        key=lambda p: p.stem.lower(),
    )
    body = "\n".join(build_icon(icon) for icon in icons)
    DST.parent.mkdir(parents=True, exist_ok=True)
    DST.write_text(HEADER + "\n" + body + HELPERS, encoding="utf-8")
    print(f"Готово: {DST.relative_to(ROOT)} ({len(icons)} иконок)")


if __name__ == "__main__":
    main()
