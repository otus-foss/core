"""Convert the brand SVGs in designsystem/brand into the design system's vector drawables.

Run it after changing a brand SVG or an AccentPreset color: python3 tools/brand_drawables.py
"""

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "designsystem"
BRAND = ROOT / "brand"
COMPOSE = ROOT / "src/commonMain/composeResources/drawable"
ANDROID = ROOT / "src/androidMain/res"

# Must match AccentPreset in theme/colors/AccentPreset.kt.
# A host test checks every preset has an icon.
ACCENTS = {
    "otus_blue": "#4D6F92",
    "sky": "#1D9BF0",
    "blue": "#2383E2",
    "violet": "#7C5CC4",
    "pink": "#C05A8F",
    "orange": "#C8613A",
    "green": "#3F8F6B",
}
MONOCHROME = "#EBECED"
ICON_BACKGROUND = "#151515"
WHITE = "#FFFFFFFF"

PATH = re.compile(r"<path ([^>]*?)/?>")
ATTR = re.compile(r'([\w-]+)="([^"]*)"')


def art(name: str) -> list[dict[str, str]]:
    """The painted paths of a brand SVG; mask and clip shapes are white boxes."""
    svg = (BRAND / name).read_text()
    found = [dict(ATTR.findall(m.group(1))) for m in PATH.finditer(svg)]
    return [p for p in found if p.get("fill") != "white"]


def vector_path(p: dict[str, str], indent: str) -> str:
    attrs = [f'android:pathData="{p["d"]}"']
    if p.get("fill", "none") != "none":
        attrs.append(f'android:fillColor="{p["fill"]}"')
    if "fill-rule" in p:
        attrs.append('android:fillType="evenOdd"')
    if "stroke" in p:
        attrs += [
            f'android:strokeColor="{p["stroke"]}"',
            f'android:strokeWidth="{p["stroke-width"]}"',
        ]
    if "stroke-linecap" in p:
        attrs.append(f'android:strokeLineCap="{p["stroke-linecap"]}"')
    if "stroke-linejoin" in p:
        attrs.append(f'android:strokeLineJoin="{p["stroke-linejoin"]}"')
    inner = f"\n{indent}    ".join(attrs)
    return f"{indent}<path\n{indent}    {inner} />"


def vector(size: float, body: list[str], dp: float | None = None) -> str:
    dp = dp or size
    return (
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        f'    android:width="{dp:g}dp"\n    android:height="{dp:g}dp"\n'
        f'    android:viewportWidth="{size:g}"\n    android:viewportHeight="{size:g}">\n'
        + "\n".join(body)
        + "\n</vector>\n"
    )


def group(attrs: dict[str, str], paths: list[dict[str, str]]) -> list[str]:
    opening = "".join(f'\n      android:{key}="{value}"' for key, value in attrs.items())
    return [f"  <group{opening}>", *(vector_path(p, "    ") for p in paths), "  </group>"]


def write(path: Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text)


def recolor(paths: list[dict[str, str]], light: str, dark: str) -> list[dict[str, str]]:
    """Paints the art's light parts [light] and its dark parts [dark]."""
    swap = {MONOCHROME: light, ICON_BACKGROUND: dark}
    return [
        {
            key: swap.get(value, value) if key in ("fill", "stroke") else value
            for key, value in p.items()
        }
        for p in paths
    ]


def logo_and_owl() -> None:
    logo = art("otus-logo.svg")
    clip = [f'    <clip-path android:pathData="{logo[0]["d"]}" />']
    body = ["  <group>", *clip, *(vector_path(p, "    ") for p in logo), "  </group>"]
    write(COMPOSE / "brand_otus_logo.xml", vector(128, body))
    owl = art("otus-owl.svg")
    write(COMPOSE / "brand_owl.xml", vector(128, [vector_path(p, "  ") for p in owl]))
    # A one-color owl for symbol slots: the body with the eyes and beak cut out, cropped to the owl.
    cutout = {
        "d": " ".join(p["d"] for p in (owl[0], owl[1], owl[2], owl[5])),
        "fill": WHITE,
        "fill-rule": "evenodd",
    }
    write(
        COMPOSE / "symbol_owl.xml",
        vector(104, group({"translateX": "-12", "translateY": "-19"}, [cutout]), 24),
    )


def launch_layers() -> None:
    """One color per layer, so the app tints each from the theme."""
    body, eye1, shine1, eye2, shine2, beak, envelope, flap = art("otus-mail-launch-once.svg")
    layers = {
        "brand_mail_owl": [body],
        "brand_mail_beak": [beak],
        "brand_mail_eye_left": [eye1],
        "brand_mail_eye_left_shine": [shine1],
        "brand_mail_eye_right": [eye2],
        "brand_mail_eye_right_shine": [shine2],
        "brand_mail_envelope": [{"d": envelope["d"], "fill": WHITE}],
        "brand_mail_envelope_lines": [
            {"d": envelope["d"], "stroke": WHITE, "stroke-width": envelope["stroke-width"]},
            {**flap, "stroke": WHITE},
        ],
    }
    for name, parts in layers.items():
        white = [{**p, "fill": WHITE} if p.get("fill", "none") != "none" else p for p in parts]
        write(COMPOSE / f"{name}.xml", vector(128, [vector_path(p, "  ") for p in white]))


def launcher_icons() -> None:
    """Mail's launcher icon in Monochrome and in each preset accent, on the dark icon background."""
    icon = art("otus-mail-icon.svg")
    # Fits the owl and envelope in the 66 dp safe circle, centered on the art's middle (63, 64.5).
    scale = 0.535
    place = {
        "scaleX": f"{scale}",
        "scaleY": f"{scale}",
        "translateX": f"{54 - 63 * scale:.3f}",
        "translateY": f"{54 - 64.5 * scale:.3f}",
    }
    for name, color in {"monochrome": MONOCHROME, **ACCENTS}.items():
        icon_name = f"otus_mail_icon_{name}"
        painted = recolor(icon, color, ICON_BACKGROUND)
        write(
            ANDROID / f"mipmap-anydpi/{icon_name}.xml",
            vector(126.075, [vector_path(p, "  ") for p in painted], 108),
        )
        write(
            ANDROID / f"drawable/{icon_name}_foreground.xml", vector(108, group(place, painted[1:]))
        )
        write(
            ANDROID / f"mipmap-anydpi-v26/{icon_name}.xml",
            '<?xml version="1.0" encoding="utf-8" ?>\n'
            '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
            '  <background android:drawable="@color/otus_brand_background" />\n'
            f'  <foreground android:drawable="@drawable/{icon_name}_foreground" />\n'
            "</adaptive-icon>\n",
        )


if __name__ == "__main__":
    logo_and_owl()
    launch_layers()
    launcher_icons()
