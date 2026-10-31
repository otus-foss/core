"""Render Mail's chrome launcher icon: the owl and envelope as polished metal on a white tile.

A vector drawable cannot shade a rounded, reflective surface, so this renders the adaptive icon's
foreground as WebP at each density. It needs rsvg-convert, NumPy, SciPy, and Pillow:
python3 tools/chrome_icon.py
"""

import io
import subprocess

import numpy as np
from brand_drawables import ANDROID, COMPOSE, ICON_PLACE, adaptive_icon, art, compose_icon, write
from PIL import Image
from scipy import ndimage

NAME = "otus_mail_icon_chrome"
DENSITIES = {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}
# Rendered at four times the largest density, then scaled down, for smooth edges.
SIZE = 4 * DENSITIES["xxxhdpi"]
PX = SIZE / 108
# How far in from an edge the metal rounds over to its flat face, in pixels.
BEVEL = 9 * PX
LIGHT = np.array([-0.45, -0.75, 0.5]) / np.linalg.norm([-0.45, -0.75, 0.5])
TILE_TOP = "#FFFFFF"
TILE_BOTTOM = "#E2E4E8"


def mask(white: list[int]) -> np.ndarray:
    """The coverage of the icon art's paths at [white] indices, from 0 to 1, at [SIZE]."""
    paths = art("otus-mail-icon.svg")
    place = (
        f"translate({float(ICON_PLACE['translateX']) * PX} {float(ICON_PLACE['translateY']) * PX}) "
        f"scale({float(ICON_PLACE['scaleX']) * PX})"
    )
    shapes = "".join(
        f'<path d="{p["d"]}" fill="{"#fff" if i in white else "#000"}" '
        f'stroke="{"#000" if "stroke" in p else "none"}" stroke-width="{p.get("stroke-width", 0)}" '
        f'stroke-linecap="round" stroke-linejoin="round"/>'
        if p.get("fill", "none") != "none"
        else f'<path d="{p["d"]}" fill="none" stroke="#000" stroke-width="{p["stroke-width"]}" '
        f'stroke-linecap="round" stroke-linejoin="round"/>'
        for i, p in enumerate(paths)
        if i > 0
    )
    svg = (
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{SIZE}" height="{SIZE}" '
        f'viewBox="0 0 {SIZE} {SIZE}"><rect width="{SIZE}" height="{SIZE}"/>'
        f'<g transform="{place}">{shapes}</g></svg>'
    )
    png = subprocess.run(
        ["rsvg-convert", "-f", "png"], input=svg.encode(), capture_output=True, check=True
    ).stdout
    return np.asarray(Image.open(io.BytesIO(png)).convert("L"), float) / 255


def height(coverage: np.ndarray, bevel: float) -> np.ndarray:
    """A surface that rounds over like a quarter circle within [bevel] of the shape's edge."""
    edge = np.clip(ndimage.distance_transform_edt(coverage > 0.5) / bevel, 0, 1)
    # A gentle swell across each face, so even the middle catches changing reflections.
    swell = ndimage.gaussian_filter(coverage, 8 * PX) * 3 * bevel
    rounded = np.sqrt(1 - (1 - edge) ** 2) * bevel + swell * edge
    return ndimage.gaussian_filter(rounded, PX / 3)


def normals(surface: np.ndarray) -> np.ndarray:
    dy, dx = np.gradient(surface)
    n = np.stack([-dx, -dy, np.ones_like(surface)], axis=-1)
    return n / np.linalg.norm(n, axis=-1, keepdims=True)


def studio(level: np.ndarray, across: np.ndarray) -> np.ndarray:
    """A studio seen in the metal: bright above, a dark horizon, a pale floor below."""
    sky = 1.0 - 0.35 * np.clip(level + 0.9, 0, 1)
    horizon = np.exp(-(((level - 0.1) / 0.22) ** 2))
    floor = 0.45 + 0.45 * np.clip(level - 0.3, 0, 1)
    tone = np.where(level < 0.1, sky, floor) * (1 - 0.95 * horizon)
    # A soft window to the upper left lights the faces that turn towards it.
    window = np.exp(-(((level + 0.55) / 0.12) ** 2) - ((across + 0.35) / 0.35) ** 2)
    return np.clip(tone + 0.6 * window, 0, 1)


def metal(n: np.ndarray) -> np.ndarray:
    """RGB of a chrome surface with normals [n]."""
    y, x = np.mgrid[0:SIZE, 0:SIZE] / SIZE - 0.5
    reflected = 2 * n[..., 2:3] * n - np.array([0, 0, 1])
    level = 1.6 * reflected[..., 1] + 1.4 * y + 0.25 * np.sin(9 * x + 4 * y)
    across = reflected[..., 0] + 0.6 * x
    tone = studio(level, across)
    shine = np.clip(reflected @ LIGHT, 0, 1) ** 40
    lum = np.clip(tone + shine, 0, 1)
    # Cool shadows and slightly warm highlights read as polished steel rather than grey paint.
    tint = np.stack([0.94 + 0.06 * lum, 0.96 + 0.04 * lum, 1.0 + 0 * lum], axis=-1)
    return np.clip(lum[..., None] * tint, 0, 1)


def glass(n: np.ndarray) -> np.ndarray:
    """RGB of dark, glossy glass, for the eyes and beak."""
    reflected = 2 * n[..., 2:3] * n - np.array([0, 0, 1])
    shine = np.clip(reflected @ LIGHT, 0, 1) ** 30
    lum = 0.05 + 0.1 * np.clip(-reflected[..., 1], 0, 1) + 0.9 * shine
    return np.clip(np.stack([lum, lum * 1.02, lum * 1.08], axis=-1), 0, 1)


def shadow(coverage: np.ndarray) -> np.ndarray:
    """The mark's soft shadow on the tile, cast down and to the right of the light."""
    far = ndimage.shift(ndimage.gaussian_filter(coverage, 5 * PX), (3 * PX, 0.8 * PX), order=1)
    near = ndimage.shift(ndimage.gaussian_filter(coverage, 1.2 * PX), (0.8 * PX, 0), order=1)
    return np.clip(0.22 * far + 0.25 * near, 0, 1)


def foreground() -> Image.Image:
    # Art paths: 1 body, 2 and 3 eyes, 4 and 5 eye shines, 6 beak, 7 envelope, 8 flap.
    chrome = mask([1, 4, 5, 7])
    dark = np.clip(mask([2, 3, 6]) - chrome, 0, 1)
    shade = metal(normals(height(chrome, BEVEL)))
    # The eyes and beak sit lower than the metal around them, as glass set into it.
    gloss = glass(normals(height(dark, BEVEL * 0.8)))
    depth = np.clip(ndimage.distance_transform_edt(chrome < 0.5) / (2 * PX), 0, 1)
    gloss *= 0.6 + 0.4 * depth[..., None]
    color = shade * chrome[..., None] + gloss * dark[..., None]
    cover = np.clip(chrome + dark, 0, 1)
    drop = shadow(cover) * (1 - cover)
    alpha = cover + drop
    rgb = np.where(alpha[..., None] > 0, color / np.maximum(alpha, 1e-6)[..., None], 0)
    rgba = np.dstack([rgb, alpha]).clip(0, 1)
    return Image.fromarray((rgba * 255).round().astype(np.uint8), "RGBA")


def main() -> None:
    image = foreground()
    for density, size in DENSITIES.items():
        path = ANDROID / f"drawable-{density}/{NAME}_foreground.webp"
        path.parent.mkdir(parents=True, exist_ok=True)
        image.resize((size, size), Image.LANCZOS).save(path, lossless=True)
    preview = DENSITIES["xxxhdpi"]
    image.resize((preview, preview), Image.LANCZOS).save(
        COMPOSE / f"{compose_icon(NAME)}.webp", lossless=True
    )
    write(
        ANDROID / f"drawable/{NAME}_background.xml",
        '<?xml version="1.0" encoding="utf-8" ?>\n'
        '<shape xmlns:android="http://schemas.android.com/apk/res/android">\n'
        f'  <gradient android:angle="270" android:startColor="{TILE_TOP}" '
        f'android:endColor="{TILE_BOTTOM}" />\n'
        "</shape>\n",
    )
    write(
        ANDROID / f"mipmap-anydpi/{NAME}.xml",
        '<?xml version="1.0" encoding="utf-8" ?>\n'
        '<layer-list xmlns:android="http://schemas.android.com/apk/res/android">\n'
        f'  <item android:drawable="@drawable/{NAME}_background" />\n'
        f'  <item android:drawable="@drawable/{NAME}_foreground" />\n'
        "</layer-list>\n",
    )
    adaptive_icon(NAME, f"@drawable/{NAME}_background")


if __name__ == "__main__":
    main()
