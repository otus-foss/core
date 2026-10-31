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


def vector_paths(paths: list[dict[str, str]], indent: str = "  ") -> list[str]:
    return [vector_path(p, indent) for p in paths]


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
    return [f"  <group{opening}>", *vector_paths(paths, "    "), "  </group>"]


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
    body = ["  <group>", *clip, *vector_paths(logo, "    "), "  </group>"]
    write(COMPOSE / "brand_otus_logo.xml", vector(128, body))
    owl = art("otus-owl.svg")
    write(COMPOSE / "brand_owl.xml", vector(128, vector_paths(owl)))
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
        write(COMPOSE / f"{name}.xml", vector(128, vector_paths(white)))


# Fits the owl and envelope in the 66 dp safe circle, centered on the art's middle (63, 64.5).
ICON_SCALE = 0.535
ICON_PLACE = {
    "scaleX": f"{ICON_SCALE}",
    "scaleY": f"{ICON_SCALE}",
    "translateX": f"{54 - 63 * ICON_SCALE:.3f}",
    "translateY": f"{54 - 64.5 * ICON_SCALE:.3f}",
}
ICON_SIZE = 126.075

# Pride flags' stripes from top to bottom; a repeated color makes a wider stripe.
PRIDE_FLAGS = {
    "rainbow": ["#E40303", "#FF8C00", "#FFED00", "#008026", "#24408E", "#732982"],
    "trans": ["#5BCEFA", "#F5A9B8", "#FFFFFF", "#F5A9B8", "#5BCEFA"],
    "bi": ["#D60270", "#D60270", "#9B4F96", "#0038A8", "#0038A8"],
    "lesbian": ["#D52D00", "#EF7627", "#FF9A56", "#FFFFFF", "#D162A4", "#B55690", "#A30262"],
}
# The art's top and bottom edge, envelope outline included, so each stripe shows on the mark.
MARK_TOP = 8.2
MARK_BOTTOM = 121.1


def compose_icon(icon_name: str) -> str:
    """otus_mail_icon_sky becomes brand_mail_icon_sky, beside the other brand drawables."""
    return icon_name.replace("otus_", "brand_", 1)


def adaptive_icon(icon_name: str, background: str) -> None:
    write(
        ANDROID / f"mipmap-anydpi-v26/{icon_name}.xml",
        '<?xml version="1.0" encoding="utf-8" ?>\n'
        '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
        f'  <background android:drawable="{background}" />\n'
        f'  <foreground android:drawable="@drawable/{icon_name}_foreground" />\n'
        "</adaptive-icon>\n",
    )


def launcher_icons() -> None:
    """Mail's launcher icon in Monochrome and in each preset accent, on the dark icon background."""
    icon = art("otus-mail-icon.svg")
    for name, color in {"monochrome": MONOCHROME, **ACCENTS}.items():
        icon_name = f"otus_mail_icon_{name}"
        painted = recolor(icon, color, ICON_BACKGROUND)
        write(
            ANDROID / f"mipmap-anydpi/{icon_name}.xml",
            vector(ICON_SIZE, vector_paths(painted), 108),
        )
        foreground = vector(108, group(ICON_PLACE, painted[1:]))
        write(ANDROID / f"drawable/{icon_name}_foreground.xml", foreground)
        # The same foreground as a Compose drawable, for the App icon setting's previews.
        write(COMPOSE / f"{compose_icon(icon_name)}.xml", foreground)
        adaptive_icon(icon_name, "@color/otus_brand_background")


def stripes(colors: list[str]) -> list[dict[str, str]]:
    """A flag over the art's mark, its stripes running across.

    Each stripe reaches the bottom and the next one covers it, so no seam shows between them.
    """
    height = (MARK_BOTTOM - MARK_TOP) / len(colors)
    return [
        {"d": f"M0 {MARK_TOP + i * height:.3f}H{ICON_SIZE}V{MARK_BOTTOM}H0Z", "fill": color}
        for i, color in enumerate(colors)
    ]


def pride_icons() -> None:
    """The owl and envelope painted in a pride flag's stripes, on the dark icon background."""
    square, body, eye1, eye2, shine1, shine2, beak, envelope, flap = recolor(
        art("otus-mail-icon.svg"), MONOCHROME, ICON_BACKGROUND
    )
    outline = {key: value for key, value in envelope.items() if key != "fill"}
    details = [eye1, eye2, shine1, shine2, beak, outline, flap]

    # One clip per shape: in one clip path, opposite windings cancel where the shapes overlap.
    def mark(colors: list[str], indent: str) -> list[str]:
        inner = indent + "  "
        flag = vector_paths(stripes(colors), inner)
        clipped = [
            [
                f"{indent}<group>",
                f'{inner}<clip-path android:pathData="{shape["d"]}" />',
                *flag,
                f"{indent}</group>",
            ]
            for shape in (body, envelope)
        ]
        return [*clipped[0], *clipped[1], *vector_paths(details, indent)]

    opening = "".join(f'\n      android:{key}="{value}"' for key, value in ICON_PLACE.items())
    for name, colors in PRIDE_FLAGS.items():
        icon_name = f"otus_mail_icon_{name}"
        write(
            ANDROID / f"mipmap-anydpi/{icon_name}.xml",
            vector(ICON_SIZE, [*vector_paths([square]), *mark(colors, "  ")], 108),
        )
        foreground = vector(108, [f"  <group{opening}>", *mark(colors, "    "), "  </group>"])
        write(ANDROID / f"drawable/{icon_name}_foreground.xml", foreground)
        write(COMPOSE / f"{compose_icon(icon_name)}.xml", foreground)
        adaptive_icon(icon_name, "@color/otus_brand_background")


if __name__ == "__main__":
    logo_and_owl()
    launch_layers()
    launcher_icons()
    pride_icons()
