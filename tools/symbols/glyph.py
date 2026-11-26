"""Signed-distance engine for Otus symbols.

Every shape is a signed distance field on the 24-unit grid: negative inside, positive outside,
measured in grid units. Booleans, smooth blends, exact rounding and knockouts all work on the
field, so every curve comes out as a true arc and every gap as an exact offset. Icons are stacks
of layers; each layer knocks out the layers below it.
"""

import math
from dataclasses import dataclass, field

import numpy as np
from scipy import ndimage
from skimage import measure

GRID = 24
RES = 48
GAP = 1.5
CUT_ROUND = 1.0
BACK_ALPHA = 0.45
MIN_WIDTH = 2.4

_N = GRID * RES
_PAD = 2 * RES
_AXIS = (np.arange(-_PAD, _N + _PAD) + 0.5) / RES
X, Y = np.meshgrid(_AXIS, _AXIS)


class Shape:
    """A lazily evaluated signed distance field."""

    def __init__(self, fn):
        self._fn = fn
        self._field = None

    def at(self, x, y):
        return self._fn(x, y)

    @property
    def field(self):
        if self._field is None:
            self._field = self._fn(X, Y)
        return self._field

    def __or__(self, other):
        return Shape(lambda x, y: np.minimum(self.at(x, y), other.at(x, y)))

    def __and__(self, other):
        return Shape(lambda x, y: np.maximum(self.at(x, y), other.at(x, y)))

    def __sub__(self, other):
        return Shape(lambda x, y: np.maximum(self.at(x, y), -other.at(x, y)))

    def blend(self, other, k):
        """Union with a smooth fillet of about k units where the two shapes meet."""

        def fn(x, y):
            a, b = self.at(x, y), other.at(x, y)
            h = np.clip(0.5 + 0.5 * (b - a) / k, 0, 1)
            return b * (1 - h) + a * h - k * h * (1 - h)

        return Shape(fn)

    def grow(self, d):
        """Exact outward offset by d units (inward when negative)."""
        return _from_mask(_mask_sdf(self.field <= 0) <= d)

    def round(self, r):
        """Rounds every corner, convex and concave, with a true arc of radius r."""
        return _from_mask(_rounded_mask(self.field <= 0, r))

    def move(self, dx, dy):
        return Shape(lambda x, y: self.at(x - dx, y - dy))

    def rotate(self, deg, origin=(12, 12)):
        c, s = math.cos(math.radians(deg)), math.sin(math.radians(deg))
        ox, oy = origin

        def fn(x, y):
            dx, dy = x - ox, y - oy
            return self.at(ox + c * dx + s * dy, oy - s * dx + c * dy)

        return Shape(fn)

    def scale(self, k, origin=(12, 12)):
        ox, oy = origin
        return Shape(lambda x, y: self.at(ox + (x - ox) / k, oy + (y - oy) / k) * k)

    def without_slivers(self, min_area):
        """Drops pieces smaller than min_area square units, such as slivers left by a slash."""
        mask = self.field <= 0
        labels, count = ndimage.label(mask)
        areas = ndimage.sum(mask, labels, range(1, count + 1)) / RES**2
        kept = [i + 1 for i, area in enumerate(areas) if area >= min_area]
        return _from_mask(mask & np.isin(labels, kept))

    def bounds(self):
        """(x0, y0, x1, y1) of the filled area in grid units."""
        rows, cols = np.nonzero(self.field <= 0)
        return (
            (cols.min() - _PAD) / RES,
            (rows.min() - _PAD) / RES,
            (cols.max() + 1 - _PAD) / RES,
            (rows.max() + 1 - _PAD) / RES,
        )

    def center_in(self, other, axis="xy"):
        """Moves this shape so its bounds are centered on the other shape's bounds."""
        ax0, ay0, ax1, ay1 = self.bounds()
        bx0, by0, bx1, by1 = other.bounds()
        dx = (bx0 + bx1 - ax0 - ax1) / 2 if "x" in axis else 0
        dy = (by0 + by1 - ay0 - ay1) / 2 if "y" in axis else 0
        return self.move(dx, dy)

    def mirror(self, origin=(12, 12)):
        return Shape(lambda x, y: self.at(2 * origin[0] - x, y))

    def flip(self, origin=(12, 12)):
        return Shape(lambda x, y: self.at(x, 2 * origin[1] - y))


def _mask_sdf(mask):
    outside = ndimage.distance_transform_edt(~mask)
    inside = ndimage.distance_transform_edt(mask)
    return np.where(mask, -(inside - 0.5), outside - 0.5) / RES


def _sample(arr):
    def fn(x, y):
        if x is X and y is Y:
            return arr
        rows = (y * RES - 0.5) + _PAD
        cols = (x * RES - 0.5) + _PAD
        return ndimage.map_coordinates(arr, [rows, cols], order=1, mode="nearest")

    return fn


def _from_mask(mask):
    return Shape(_sample(_mask_sdf(mask)))


def _rounded_mask(mask, r):
    """Closing then opening by r: fills concave corners, then trims convex ones."""
    if r <= 0:
        return mask
    closed = _mask_sdf(_mask_sdf(mask) <= r) <= -r
    return _mask_sdf(_mask_sdf(closed) <= -r) <= r


# Primitives. Every distance is exact.


def circle(cx, cy, r):
    return Shape(lambda x, y: np.hypot(x - cx, y - cy) - r)


def ring(cx, cy, r, width):
    return Shape(lambda x, y: np.abs(np.hypot(x - cx, y - cy) - r) - width / 2)


def rbox(x0, y0, w, h, r=0):
    """Rounded box with its top-left corner at (x0, y0); r is clamped to half the short side."""
    r = min(r, w / 2, h / 2)
    cx, cy, hx, hy = x0 + w / 2, y0 + h / 2, w / 2 - r, h / 2 - r

    def fn(x, y):
        qx, qy = np.abs(x - cx) - hx, np.abs(y - cy) - hy
        outside = np.hypot(np.maximum(qx, 0), np.maximum(qy, 0))
        return outside + np.minimum(np.maximum(qx, qy), 0) - r

    return Shape(fn)


def pill(x0, y0, w, h):
    return rbox(x0, y0, w, h, min(w, h) / 2)


def _segment(x, y, a, b):
    ax, ay = a
    bx, by = b
    dx, dy = bx - ax, by - ay
    length2 = dx * dx + dy * dy or 1e-12
    t = np.clip(((x - ax) * dx + (y - ay) * dy) / length2, 0, 1)
    return np.hypot(x - ax - t * dx, y - ay - t * dy)


def stroke(points, width):
    """A polyline drawn with round caps and joins."""

    def fn(x, y):
        d = _segment(x, y, points[0], points[1])
        for a, b in zip(points[1:], points[2:], strict=False):
            d = np.minimum(d, _segment(x, y, a, b))
        return d - width / 2

    return Shape(fn)


def poly(points):
    """A filled polygon with sharp corners; call .round() on it."""
    pts = list(points)

    def fn(x, y):
        d = np.full(np.shape(x), np.inf)
        inside = np.zeros(np.shape(x), dtype=bool)
        for (ax, ay), (bx, by) in zip(pts, pts[1:] + pts[:1], strict=True):
            d = np.minimum(d, _segment(x, y, (ax, ay), (bx, by)))
            crosses = (ay > y) != (by > y)
            hit = x < (bx - ax) * (y - ay) / ((by - ay) or 1e-12) + ax
            inside ^= crosses & hit
        return np.where(inside, -d, d)

    return Shape(fn)


def arc(cx, cy, r, start_deg, end_deg, steps=64):
    """Points along a circle; 0 degrees points right, 90 points down."""
    return [
        (
            cx + r * math.cos(math.radians(start_deg + (end_deg - start_deg) * i / steps)),
            cy + r * math.sin(math.radians(start_deg + (end_deg - start_deg) * i / steps)),
        )
        for i in range(steps + 1)
    ]


def union(*shapes):
    out = shapes[0]
    for s in shapes[1:]:
        out = out | s
    return out


# Icons as layer stacks.


@dataclass
class Layer:
    shape: Shape
    back: bool
    knocks: bool


@dataclass
class Icon:
    """Layers listed bottom to top. A layer knocks a GAP-wide gap out of every layer below it;
    a mark sits on the layers below without a gap."""

    layers: list = field(default_factory=list)
    keyline: bool = True

    def free(self):
        """Keeps the drawn size instead of scaling to a keyline, for glyphs like a minus."""
        self.keyline = False
        return self

    def back(self, shape):
        self.layers.append(Layer(shape, back=True, knocks=True))
        return self

    def front(self, shape):
        self.layers.append(Layer(shape, back=False, knocks=True))
        return self

    def mark(self, shape):
        self.layers.append(Layer(shape, back=False, knocks=False))
        return self


@dataclass
class Built:
    back: np.ndarray | None
    front: np.ndarray | None
    offset: tuple
    problems: list


KEYLINE_CIRCLE = 20
KEYLINE_SQUARE = 18
KEYLINE_LONG = 20
KEYLINE_SHORT = 16
KEYLINE_SCALE = (0.8, 1.25)


def keyline_scale(mask):
    """Scale that fits a silhouette to its keyline, like Material's: a 20-unit circle for round
    icons, an 18-unit square for boxy ones, and 20 x 16 for wide or tall ones. Thin glyphs, such
    as a minus or three dots, keep their size."""
    rows, cols = np.nonzero(mask)
    w = (cols.max() - cols.min() + 1) / RES
    h = (rows.max() - rows.min() + 1) / RES
    if min(w, h) < 12:
        return 1.0
    long_side, short_side = max(w, h), min(w, h)
    if long_side / short_side > 1.15:
        target = min(KEYLINE_LONG / long_side, KEYLINE_SHORT / short_side)
    else:
        boxy = mask.sum() / RES**2 / (w * h) > 0.86
        target = (KEYLINE_SQUARE if boxy else KEYLINE_CIRCLE) / long_side
    return float(np.clip(target, *KEYLINE_SCALE))


def build(icon):
    if icon.keyline:
        silhouette = np.logical_or.reduce([layer.shape.field <= 0 for layer in icon.layers])
        k = keyline_scale(silhouette)
        if abs(k - 1) > 0.02:
            rows, cols = np.nonzero(silhouette)
            center = (
                ((cols.min() + cols.max() + 1) / 2 - _PAD) / RES,
                ((rows.min() + rows.max() + 1) / 2 - _PAD) / RES,
            )
            scaled = [
                Layer(lay.shape.scale(k, center), lay.back, lay.knocks) for lay in icon.layers
            ]
            icon = Icon(scaled, keyline=False)
    resolved, resolved_notes = [], []
    for i, layer in enumerate(icon.layers):
        above = [u for u in icon.layers[i + 1 :] if u.knocks]
        mask = layer.shape.field <= 0
        if above:
            keep_out = np.minimum.reduce([u.shape.field for u in above])
            mask = mask & (_mask_sdf(keep_out <= 0) > GAP)
            mask = _rounded_mask(mask, CUT_ROUND)
            mask = mask & (_mask_sdf(keep_out <= 0) > GAP - 0.5 / RES)
        parts_before = ndimage.label(layer.shape.field <= 0)[1]
        parts_after = ndimage.label(mask)[1]
        if above and parts_after > parts_before:
            note = f"layer {i} split into {parts_after} parts by the layers above"
            resolved_notes.append(note)
        resolved.append((layer, mask))
    backs = [m for layer, m in resolved if layer.back]
    fronts = [m for layer, m in resolved if not layer.back]
    back = np.logical_or.reduce(backs) if backs else None
    front = np.logical_or.reduce(fronts) if fronts else None
    whole = np.logical_or.reduce([m for _, m in resolved])
    rows, cols = np.nonzero(whole)
    x0, x1 = (cols.min() - _PAD) / RES, (cols.max() + 1 - _PAD) / RES
    y0, y1 = (rows.min() - _PAD) / RES, (rows.max() + 1 - _PAD) / RES
    offset = (GRID / 2 - (x0 + x1) / 2, GRID / 2 - (y0 + y1) / 2)
    problems = list(resolved_notes)
    w, h = x1 - x0, y1 - y0
    if max(w, h) > GRID - 2:
        problems.append(f"too big: {w:.1f} x {h:.1f}")
    for name, m in (("back", back), ("front", front)):
        if m is None:
            continue
        thin = m & ~(_mask_sdf(_mask_sdf(m) <= -MIN_WIDTH / 2) <= MIN_WIDTH / 2)
        area = thin.sum() / RES**2
        if area > 0.15:
            problems.append(f"{name} thinner than {MIN_WIDTH} ({area:.2f} sq units)")
    return Built(back, front, offset, problems)


# Export: trace the outline, smooth away the tracing noise, and fit cubic Beziers to it.


def _contours(mask, offset):
    sdf = ndimage.gaussian_filter(_mask_sdf(mask), 0.6)
    out = []
    for c in measure.find_contours(sdf, 0):
        pts = np.column_stack([(c[:, 1] + 0.5 - _PAD) / RES, (c[:, 0] + 0.5 - _PAD) / RES])
        pts += offset
        if len(pts) > 8:
            out.append(_smooth_loop(pts[:-1] if np.allclose(pts[0], pts[-1]) else pts))
    return out


def _smooth_loop(pts, step=0.05, sigma=0.12):
    """Resamples a closed outline evenly and blurs it along its length to remove tracing noise."""
    loop = np.vstack([pts, pts[:1]])
    d = np.r_[0, np.cumsum(np.hypot(*np.diff(loop, axis=0).T))]
    n = max(16, int(d[-1] / step))
    s = np.linspace(0, d[-1], n, endpoint=False)
    even = np.column_stack([np.interp(s, d, loop[:, 0]), np.interp(s, d, loop[:, 1])])
    return ndimage.gaussian_filter1d(even, sigma / (d[-1] / n), axis=0, mode="wrap")


def _unit(v):
    return v / (np.hypot(*v) or 1)


def _bezier(ctrl, t):
    mt = 1 - t
    return (
        (mt**3)[:, None] * ctrl[0]
        + (3 * mt**2 * t)[:, None] * ctrl[1]
        + (3 * mt * t**2)[:, None] * ctrl[2]
        + (t**3)[:, None] * ctrl[3]
    )


def _fit(pts, t0, t1, tol, window):
    """Schneider's least-squares cubic fit, split at the worst point until within tol."""
    d = np.r_[0, np.cumsum(np.hypot(*np.diff(pts, axis=0).T))]
    u = d / d[-1]
    a0 = (3 * (1 - u) ** 2 * u)[:, None] * t0
    a1 = (3 * (1 - u) * u**2)[:, None] * t1
    rhs = pts - _bezier(np.array([pts[0], pts[0], pts[-1], pts[-1]]), u)
    c00, c01, c11 = (a0 * a0).sum(), (a0 * a1).sum(), (a1 * a1).sum()
    x0, x1 = (a0 * rhs).sum(), (a1 * rhs).sum()
    det = c00 * c11 - c01 * c01
    la, lb = ((x0 * c11 - x1 * c01) / det, (c00 * x1 - c01 * x0) / det) if det else (0, 0)
    if la < 0.05 * d[-1] or lb < 0.05 * d[-1]:
        la = lb = d[-1] / 3
    ctrl = np.array([pts[0], pts[0] + la * t0, pts[-1] + lb * t1, pts[-1]])
    err = np.hypot(*(_bezier(ctrl, u) - pts).T)
    worst = int(err.argmax())
    if err[worst] <= tol or len(pts) < 4 * window:
        return [ctrl]
    worst = min(max(worst, window), len(pts) - 1 - window)
    tangent = _unit(pts[worst + window] - pts[worst - window])
    left = _fit(pts[: worst + 1], t0, -tangent, tol, window)
    return left + _fit(pts[worst:], tangent, t1, tol, window)


def path(mask, offset, tol=0.01):
    """Cubic Bezier outline of a mask, fitted to its smoothed trace."""
    if mask is None:
        return ""
    out = []
    for loop in _contours(mask, offset):
        window = 3
        closed = np.vstack([loop, loop[:1]])
        tangent = _unit(loop[window] - loop[-window])
        curves = _fit(closed, tangent, -tangent, tol, window)
        out.append(f"M{curves[0][0][0]:.3f} {curves[0][0][1]:.3f}")
        out.extend("C" + " ".join(f"{p[0]:.3f} {p[1]:.3f}" for p in c[1:]) for c in curves)
        out.append("Z")
    return "".join(out)
