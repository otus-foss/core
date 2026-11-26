"""Writes the distance-field symbols as Android vector drawables.

    python export.py DRAWABLE_DIR

Each icon in glyphs/ becomes DRAWABLE_DIR/symbol_<name>.xml with up to two paths: the back path
at BACK_ALPHA and the front path at full strength, both white so `Symbol`'s tint colors them.
Other files, such as the brand owl, are left untouched.
"""

import pathlib
import sys

from glyph import BACK_ALPHA, build, path
from glyphs import load

HEADER = (
    '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
    '    android:width="24dp"\n'
    '    android:height="24dp"\n'
    '    android:viewportWidth="24"\n'
    '    android:viewportHeight="24">\n'
)


def path_element(data, alpha=None):
    fill_alpha = f'\n      android:fillAlpha="{alpha}"' if alpha is not None else ""
    return (
        "  <path\n"
        '      android:fillColor="#FFFFFFFF"'
        f"{fill_alpha}\n"
        '      android:fillType="evenOdd"\n'
        f'      android:pathData="{data}"/>\n'
    )


def main():
    out = pathlib.Path(sys.argv[1])
    icons = load()
    problems = []
    for name, make in sorted(icons.items()):
        built = build(make())
        back = path(built.back, built.offset)
        front = path(built.front, built.offset)
        problems += [f"{name}: {p}" for p in built.problems if "split" in p or "too big" in p]
        body = (path_element(back, BACK_ALPHA) if back else "") + (
            path_element(front) if front else ""
        )
        (out / f"symbol_{name}.xml").write_text(f"{HEADER}{body}</vector>\n")
    print(f"wrote {len(icons)} symbols")
    for p in problems:
        print("problem:", p)


if __name__ == "__main__":
    main()
