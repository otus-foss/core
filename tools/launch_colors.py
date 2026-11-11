"""Write an app's launch-window colors from the design system's default theme.

Android draws the window before Compose starts, from androidApp/src/main/res/values/colors.xml, so
that file repeats the Otus theme's page and text colors and the default accent. The architecture
check fails when it drifts. Run it from an app's root after changing those tokens:
python3 ../otus-core/tools/launch_colors.py
"""

import re
import sys
from pathlib import Path

CORE = Path(__file__).resolve().parents[1]
COLORS = CORE / "designsystem/src/commonMain/kotlin/com/otusfoss/core/designsystem/theme/colors"
LAUNCH_COLORS = Path("androidApp/src/main/res/values/colors.xml")


def _hex(argb: str) -> str:
    return "#" + argb[2:].upper() if argb.upper().startswith("FF") else "#" + argb.upper()


def theme_colors() -> dict[str, str]:
    """The window resources and the theme tokens they take their color from."""
    base = (COLORS / "ThemeBase.kt").read_text()
    otus = re.search(r"\n    Otus\((.*?)\n    \)", base, re.DOTALL).group(1)
    accent = re.search(r"OtusBlue\(Color\(0x(\w{8})\)\)", (COLORS / "AccentPreset.kt").read_text())
    token = dict(re.findall(r"(\w+) = Color\(0x(\w{8})\)", otus))
    return {
        "otus_bg_regular": _hex(token["page"]),
        "otus_text_1": _hex(token["text1"]),
        "otus_accent_regular": _hex(accent.group(1)),
    }


def launch_colors_xml() -> str:
    rows = "".join(
        f'  <color name="{name}">{color}</color>\n' for name, color in theme_colors().items()
    )
    return (
        '<?xml version="1.0" encoding="utf-8" ?>\n'
        "<!-- Written by otus-core's tools/launch_colors.py from the Otus theme; do not edit. -->\n"
        f"<resources>\n{rows}</resources>\n"
    )


def launch_color_violations(root: Path) -> list[str]:
    path = root / LAUNCH_COLORS
    if path.is_file() and path.read_text() == launch_colors_xml():
        return []
    return [f"{LAUNCH_COLORS}: differs from Core's theme; run otus-core's tools/launch_colors.py"]


if __name__ == "__main__":
    target = Path(sys.argv[1] if len(sys.argv) > 1 else ".") / LAUNCH_COLORS
    target.write_text(launch_colors_xml())
    print(f"Wrote {target}")
