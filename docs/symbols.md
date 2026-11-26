# Symbols

Otus draws its own symbols: solid, rounded, two-tone shapes on a 24-unit grid, with no outlines.
A Python generator in `tools/symbols/` builds them and writes the vector drawables in
`designsystem/src/commonMain/composeResources/drawable/symbol_<name>.xml`. Never edit those files
by hand. Change the generator and export again.

The brand owl (`symbol_owl`) is not generated. It comes from the brand SVGs; see
[the design system guide](designsystem.md#brand).

## Generate the symbols

The generator needs Python 3.11 or later. Set up its environment once:

```sh
python3 -m venv ~/.venvs/otus-symbols
~/.venvs/otus-symbols/bin/pip install -r tools/symbols/requirements.txt
```

Then, from `tools/symbols/`:

1. Preview the icons you changed. `preview.py` prints each icon's problems and writes two review
   files:

   ```sh
   ~/.venvs/otus-symbols/bin/python preview.py /tmp/symbols mail_actions
   ```

   `sheet.html` shows every icon at 48 and 20 dp on a dark and a light page. `zoom.svg` shows
   every icon about 12 times larger, so you can check edges and gaps. Leave out the module name
   to preview all of them.

2. Export the drawables. This writes every symbol and takes a few minutes:

   ```sh
   ~/.venvs/otus-symbols/bin/python export.py ../../designsystem/src/commonMain/composeResources/drawable
   ```

3. Record the screenshot baselines, look at each changed PNG, and commit them with the
   drawables. See [screenshot tests](designsystem.md#screenshot-tests).

## Add or change a symbol

Symbols are grouped in `tools/symbols/glyphs/`, one module per group: `mail_actions`, `organize`,
`controls`, `content_system`, `people_media`, and the folder picker's `picker_a` and `picker_b`.
Each module has an `ICONS` dict that maps a drawable name, without `symbol_`, to a function that
returns an `Icon`.

To add a symbol, write its function in the right module, add it to `ICONS`, preview it, and
export. The export writes `symbol_<name>.xml`, and Compose generates `Res.drawable.symbol_<name>`
from it. A new symbol is a public API change, so add it to `CHANGELOG.md`.

### Layers

An icon is a stack of layers, listed bottom to top:

```python
Icon().back(body).front(flap).mark(check.center_in(body))
```

| Layer | Tone | Effect on the layers below |
| --- | --- | --- |
| `back(shape)` | Light (`BACK_ALPHA`, 45%) | Knocks out a `GAP` gap |
| `front(shape)` | Full | Knocks out a `GAP` gap |
| `mark(shape)` | Full | Sits on them with no gap |

The knockout grows the upper shape by `GAP` (1.5 units), cuts that out of every layer below, and
rounds the cut corners with a radius of `CUT_ROUND` (1 unit). Never cut a gap by hand.

### Shapes

Every shape is a signed distance field, so booleans, offsets, and rounding are exact and every
curve comes out as a true arc.

| Function | Shape |
| --- | --- |
| `circle(cx, cy, r)` | Circle |
| `ring(cx, cy, r, width)` | Ring |
| `rbox(x, y, w, h, r)` | Rounded box with its top-left corner at (x, y) |
| `pill(x, y, w, h)` | Box with fully round ends |
| `stroke(points, width)` | Polyline with round caps and joins; use `arc()` for curves |
| `poly(points)` | Polygon with sharp corners; always call `.round(r)` on it |

Combine and adjust them:

- `a | b` joins shapes, `a & b` keeps their overlap, and `a - b` cuts `b` out of `a`.
- `a.blend(b, k)` joins with a smooth fillet of about `k` units.
- `.round(r)` rounds every corner, convex and concave, with a radius of `r`.
- `.grow(d)` offsets the outline outward, or inward when `d` is negative.
- `.move`, `.rotate`, `.scale`, `.mirror`, and `.flip` transform a shape. Move a shape into place
  before you round it, because rounding works on the visible grid.
- `.center_in(other)` centers a mark on its body. Use it instead of placing marks by eye.
- `.without_slivers(min_area)` drops pieces left over from a cut.

### Keylines

The build scales each icon to a keyline before the knockouts, so the gap stays 1.5 units at every
size. It picks the keyline from the silhouette:

| Silhouette | Keyline |
| --- | --- |
| Round | 20-unit circle |
| Boxy, filling most of its bounds | 18-unit square |
| Wide or tall, sides more than 15% apart | 20 × 16 |
| Thin, with a side under 12 units, such as a chevron or a minus | Drawn size |

The scale stays between 0.8 and 1.25. Call `Icon().free()` to keep an icon's drawn size.

## Style rules

### Tones

- One color per symbol: back parts at 45%, front parts and marks at full strength.
- Use two tones only where the object has two real parts, such as a lid and a box, a folder's tab
  and pocket, or a pole and a flag. An object in one piece, such as send, reply, or a star, is one
  front layer.
- A small mark on a body, such as "!", a label hole, a slot, or clock hands, is a `mark`.
- Back parts never overlap each other, or the overlap would draw darker.

### Status marks

A status mark, such as the read check, the unread dot, or the unsubscribe minus, is a front layer
at a corner of the full-size main shape. It knocks out the main shape with the standard gap. Do
not put it on a badge disc, and never shrink the main shape to make room.

When a front part has an opening on its edge, such as a slot running in from a pocket's top edge,
knock out the back part with the outline before you cut the opening. Otherwise the gap follows the
opening and leaves a dent in the back part.

### Spacing

- Parallel strokes, such as two arrows, keep at least 3 units of empty space between them. Tight
  nested curves, such as a paper clip's legs, keep about 2 units.
- A strip of material between two holes or cuts is at least 2.5 units wide. A thinner strip reads
  as a stray line.
- A mark or slash inside a round body stays about 3.5 units from the body's edge and never touches
  it.
- A part that touches a round part, such as a handle beside a lens, overlaps it slightly, so the
  knockout leaves a small curved dent, as in `search`.

### Shape

- Make shapes fat and round. Nothing is thinner than about 2.5 units, and strokes are 3 to 4.2
  units wide.
- Large outer corners use a radius of 4.5 to 5.5. Small parts round fully.
- Build a side that should be one curve from the shape's own circle. A rounded box has straight
  sides, so a cloud built on one climbs at an angle.
- Keep the idea of the Material Symbol the icon replaces, but drop details that vanish at 20 dp.
- A `_fill` symbol draws the same as its base symbol, because every symbol is filled.

## Checks

`preview.py` prints `ok` or a list of problems for each icon:

| Problem | Meaning |
| --- | --- |
| `layer N split into M parts by the layers above` | A knockout cut a part in two. Move the parts apart or change the shape. A slash is meant to split, so `cloud_off` and `visibility_off` keep this note. |
| `too big` | The icon is wider or taller than 22 units. |
| `back thinner than 2.4` or `front thinner than 2.4` | Some material is thinner than `MIN_WIDTH`. Rounded tips and cut ends often cause small areas; look at `zoom.svg` and accept them only when they read well. |

## Tones in the app

`Symbol` draws a symbol in one of three tones:

| Tone | Drawing | Used for |
| --- | --- | --- |
| `SymbolTone.Duo` (default) | The drawable as exported: back parts at 45% of the content color | Every uncolored symbol |
| `SymbolTone.Shaded` | Both parts opaque: the front in the content color, the back in that color mixed with `bgRegular` | Colored folders, through `LocalIconTint` and `FolderGlyph` |
| `SymbolTone.Solid` | Both parts opaque in the content color, keeping the gaps | Symbols on a colored circle, such as avatar symbols |

`Shaded` and `Solid` repaint the loaded `ImageVector` instead of using other drawables, so edges
and gaps match `Duo` exactly. All three keep `LocalContentAlpha`, so disabled symbols dim.
