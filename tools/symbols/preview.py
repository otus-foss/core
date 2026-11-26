"""Builds review sheets for the symbols.

    python preview.py OUT_DIR [module ...]

Prints each icon's problems, then writes OUT_DIR/sheet.html (48 and 20 dp on a dark and a light
page) and OUT_DIR/zoom.svg (every icon large, to check edges).
"""

import pathlib
import sys

from glyph import BACK_ALPHA, build, path
from glyphs import load

THEMES = (("#161617", "#EBECED"), ("#F1F1F2", "#1B1B1C"))
CSS = (
    "<style>body{margin:0}.box{border-radius:12px;padding:16px;margin-bottom:12px}"
    "svg{fill:currentColor}.g{display:grid;gap:12px 4px;"
    "grid-template-columns:repeat(auto-fill,minmax(76px,1fr))}"
    ".s{margin-top:14px;grid-template-columns:repeat(auto-fill,minmax(36px,1fr))}"
    ".c{display:flex;flex-direction:column;align-items:center;gap:5px}"
    ".c i{font-style:normal;font-size:9px;opacity:.55;text-align:center;word-break:break-all}"
    "</style>"
)
ZOOM_COLUMNS = 5
ZOOM_CELL = 26


def paths(back, front):
    return (
        f'<path fill-rule="evenodd" opacity="{BACK_ALPHA}" d="{back}"/>'
        f'<path fill-rule="evenodd" d="{front}"/>'
    )


def cell(name, drawn, size, label):
    caption = f"<i>{name}</i>" if label else ""
    svg = f'<svg width="{size}" height="{size}" viewBox="0 0 24 24">{paths(*drawn)}</svg>'
    return f'<div class="c">{svg}{caption}</div>'


def sheet(drawn):
    def grid(size, label, extra=""):
        cells = "".join(cell(name, d, size, label) for name, d in drawn.items())
        return f'<div class="g{extra}">{cells}</div>'

    boxes = "".join(
        f'<div class="box" style="background:{bg};color:{fg}">'
        f"{grid(48, True)}{grid(20, False, ' s')}</div>"
        for bg, fg in THEMES
    )
    return (
        f'<!doctype html><html><head><meta charset="utf-8"><title>Symbols</title>{CSS}</head>'
        f"<body>{boxes}</body></html>"
    )


def zoom(drawn):
    width = ZOOM_COLUMNS * ZOOM_CELL
    height = -(-len(drawn) // ZOOM_COLUMNS) * ZOOM_CELL
    cells = "".join(
        f'<g transform="translate({i % ZOOM_COLUMNS * ZOOM_CELL} {i // ZOOM_COLUMNS * ZOOM_CELL})">'
        f"{paths(*d)}</g>"
        for i, d in enumerate(drawn.values())
    )
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{width * 12}" height="{height * 12}" '
        f'viewBox="0 0 {width} {height}"><rect width="{width}" height="{height}" fill="#fff"/>'
        f'<g fill="#111">{cells}</g></svg>'
    )


def main():
    out = pathlib.Path(sys.argv[1])
    out.mkdir(parents=True, exist_ok=True)
    drawn = {}
    for name, make in load(sys.argv[2:]).items():
        built = build(make())
        drawn[name] = (path(built.back, built.offset), path(built.front, built.offset))
        print(f"{name:24} {'; '.join(built.problems) or 'ok'}")
    (out / "sheet.html").write_text(sheet(drawn))
    (out / "zoom.svg").write_text(zoom(drawn))


if __name__ == "__main__":
    main()
