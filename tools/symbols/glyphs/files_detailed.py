"""Detailed file-type symbols for files shown 40 dp and up, beside their name, in the manner of
macOS document icons. The page keeps the draft symbol's fold, the type gets finer 1.5-unit strokes
and more parts than the plain file symbols in files.py, and it sits in the page's upper half,
leaving the lower part for the file's extension, which `FileIcon` writes there."""

from glyph import Icon, arc, circle, pill, poly, rbox, ring, stroke, union

T = 1.5


# The page's top end corner is cut along x - y = CUT, which meets its top edge at (15, 2.5) and
# its side at (20, 7.5). The fold fills that corner, so its long edge is the cut itself.
CUT = 12.5


def page():
    return rbox(4, 2.5, 16, 19, 4.5) - poly([(CUT, 0), (24, 0), (24, 24 - CUT)])


def fold():
    return poly([(2.5 + CUT, 2.5), (20, 20 - CUT), (2.5 + CUT, 20 - CUT)]).round(0.8)


def ln(x, y, w, t=T):
    return pill(x, y - t / 2, w, t)


def lines(ys, ws, x=7.5):
    return union(*[ln(x, y, w) for y, w in zip(ys, ws, strict=True)])


M = {
    "pdf": lambda: lines((11.2, 13.4), (6, 9)) | rbox(6.6, 15.4, 10.8, 4.4, 1.6),
    "document": lambda: rbox(7.2, 10.2, 5.2, 2.4, 1) | lines((14, 16.2, 18.4), (9, 9, 5.6), x=7.2),
    "text": lambda: lines((10.8, 13, 15.2, 17.4, 19.6), (6.4, 9, 9, 9, 5)),
    "sheet": lambda: (
        union(
            *[rbox(x, y, 2.6, 1.7, 0.5) for x in (7, 10.3, 13.6) for y in (10.4, 12.8, 15.2, 17.6)]
        )
        | rbox(16.2, 10.4, 0.8, 0.1, 0)
    ),
    "data": lambda: (
        stroke([(9.6, 11), (8.6, 11.6), (8.6, 14), (7.4, 15), (8.6, 16), (8.6, 18.4), (9.6, 19)], T)
        | stroke(
            [
                (14.4, 11),
                (15.4, 11.6),
                (15.4, 14),
                (16.6, 15),
                (15.4, 16),
                (15.4, 18.4),
                (14.4, 19),
            ],
            T,
        )
        | circle(11, 15, 0.95)
        | circle(13, 15, 0.95)
    ),
    "slides": lambda: (
        ln(6.8, 19.4, 10.4, 1.3)
        | union(
            *[
                pill(x - 0.8, 17.6 - h, 1.6, h)
                for x, h in ((8.4, 3.6), (10.8, 6), (13.2, 4.6), (15.6, 7.2))
            ]
        )
    ),
    "image": lambda: (
        (rbox(6.6, 10.6, 10.8, 9, 1.8) - rbox(8, 12, 8, 6.2, 0.8))
        | poly([(8, 18.2), (11, 13.8), (14, 18.2)]).round(0.5)
        | poly([(12, 18.2), (14.2, 15.4), (16, 18.2)]).round(0.5)
        | circle(14.3, 13.6, 1)
    ),
    "design": lambda: (
        stroke(arc(16.6, 19.6, 8.4, 182, 268), T)
        | stroke([(8.2, 11.4), (15.8, 11.4)], 1)
        | rbox(6.8, 17.2, 2.8, 2.8, 0.7)
        | rbox(14.6, 9.8, 2.8, 2.8, 0.7)
        | circle(8.2, 11.4, 1.1)
    ),
    "video": lambda: (
        (rbox(6.6, 10.8, 10.8, 8.6, 2) - rbox(8.2, 12.4, 7.6, 5.4, 1))
        | poly([(10.6, 13.3), (13.8, 15.1), (10.6, 16.9)]).round(0.4)
    ),
    "audio": lambda: (
        circle(8.8, 17.8, 1.9)
        | circle(14.6, 16.6, 1.9)
        | stroke([(10.1, 17.8), (10.1, 11.6), (15.9, 10.4), (15.9, 16.6)], T)
        | stroke([(10.1, 13.3), (15.9, 12.1)], T)
    ),
    "archive": lambda: (
        union(*[rbox(11.9 + (0.0 if i % 2 else -1.6), 10 + i * 1.3, 1.7, 1, 0.4) for i in range(6)])
        | (rbox(9.9, 17.8, 4.2, 3.0, 1) - rbox(11.2, 18.9, 1.6, 0.9, 0.4))
    ),
    "disk_image": lambda: (
        ring(12, 15.2, 4.1, T) | circle(12, 15.2, 1.2) | stroke(arc(12, 15.2, 2.2, 200, 250), 0.9)
    ),
    "calendar": lambda: (
        (rbox(6.8, 11.2, 10.4, 8.6, 2) - rbox(8.2, 14.2, 7.6, 4.2, 0.8))
        | pill(8.8, 9.8, 1.5, 2.8)
        | pill(13.7, 9.8, 1.5, 2.8)
        | union(*[rbox(x, y, 1.6, 1.3, 0.4) for x in (8.9, 11.2, 13.5) for y in (14.8, 16.7)])
    ),
    "contact": lambda: (
        circle(12, 12.6, 2.3)
        | (circle(12, 20.4, 5) & rbox(4, 15.6, 16, 2.6, 0)).round(0.6)
        | ln(9, 19.6, 6)
    ),
    "email": lambda: (
        (rbox(6.6, 11.4, 10.8, 7.8, 1.8) - rbox(8, 12.8, 8, 5, 0.6))
        | stroke([(8, 12.9), (12, 15.8), (16, 12.9)], T)
    ),
    "web": lambda: (
        ring(12, 15.2, 4.6, 1.3)
        | stroke([(7.6, 15.2), (16.4, 15.2)], 1.1)
        | stroke([(8.4, 12.8), (15.6, 12.8)], 1)
        | stroke([(8.4, 17.6), (15.6, 17.6)], 1)
        | stroke(arc(16.6, 15.2, 5.6, 145, 215), 1.1)
        | stroke(arc(7.4, 15.2, 5.6, -35, 35), 1.1)
    ),
    "code": lambda: (
        stroke([(9.4, 12), (7, 15), (9.4, 18)], T)
        | stroke([(14.6, 12), (17, 15), (14.6, 18)], T)
        | stroke([(13.1, 11.4), (10.9, 18.6)], T)
    ),
    "key": lambda: (
        ring(9.6, 12.8, 2.6, T)
        | stroke([(11.4, 14.6), (16.4, 19.6)], T)
        | stroke([(14.1, 17.3), (15.6, 15.8)], 1.4)
        | stroke([(15.6, 18.8), (16.9, 17.5)], 1.4)
    ),
    "signature": lambda: (
        stroke(
            [
                (7.4, 16.4),
                (8.6, 12.6),
                (9.6, 16.8),
                (11, 13),
                (12, 16.6),
                (13.6, 14.4),
                (16.6, 15.2),
            ],
            1.3,
        )
        | ln(7, 19.2, 10, 1.2)
        | stroke([(7.2, 9.8), (9, 11.6)], 1.1)
        | stroke([(9, 9.8), (7.2, 11.6)], 1.1)
    ),
    "encrypted": lambda: (
        stroke([(9.6, 14.6), (9.6, 12.6)] + arc(12, 12.6, 2.4, 180, 360)[1:] + [(14.4, 14.6)], T)
        | (rbox(7.6, 14.2, 8.8, 6, 1.6) - (circle(12, 16.6, 1) | rbox(11.5, 16.6, 1, 2, 0.4)))
    ),
    "app": lambda: (
        union(*[rbox(x, y, 2.8, 2.8, 0.9) for x in (7, 10.6, 14.2) for y in (10.6, 14.2)])
        | union(*[rbox(x, 17.8, 2.8, 2.8, 0.9) for x in (7, 10.6)])
        | circle(15.6, 19.2, 1.4)
    ),
    "font": lambda: (
        stroke([(7, 19.2), (10, 11), (13, 19.2)], T)
        | stroke([(8.2, 16.2), (11.8, 16.2)], 1.3)
        | ring(15.1, 17.4, 1.7, 1.3)
        | stroke([(16.8, 15.4), (16.8, 19.2)], 1.3)
    ),
    "ebook": lambda: (
        poly([(6.6, 11.2), (11.4, 12.2), (11.4, 19.8), (6.6, 18.8)]).round(0.8)
        - (ln(7.9, 13.6, 2.4, 0.8) | ln(7.9, 15.4, 2.4, 0.8))
        | poly([(17.4, 11.2), (12.6, 12.2), (12.6, 19.8), (17.4, 18.8)]).round(0.8)
        - (ln(13.7, 13.6, 2.4, 0.8) | ln(13.7, 15.4, 2.4, 0.8))
    ),
    "location": lambda: (
        (
            (circle(12, 13.6, 3.3) | poly([(9.2, 15.4), (14.8, 15.4), (12, 18.8)]).round(0.6))
            - circle(12, 13.6, 1.2)
        )
        | (rbox(8, 19.2, 8, 1.2, 0.6))
    ),
    "database": lambda: union(
        *[(rbox(7.4, y, 9.2, 2.6, 1.3) - ln(13.4, y + 1.3, 1.6, 0.6)) for y in (10.6, 13.9, 17.2)]
    ),
}


# Marks are drawn about y 15.2; they shrink to SCALE and move to CENTER, between the fold and the
# extension.
SCALE = 0.6
CENTER = 13.0

# The gap between the page and its fold. Narrower than the kit's GAP, which is sized for 20 dp
# and looks wide at the 40 dp and up these symbols are shown at.
FOLD_GAP = 0.8


def body():
    return (page() - fold().grow(FOLD_GAP)).round(0.6)


def placed(kind):
    return M[kind]().scale(SCALE, (12, 15)).move(0, CENTER - 15.2)


def detailed_icon(kind):
    return lambda: Icon().back(body()).mark(fold()).mark(placed(kind))


ICONS = {f"file_{kind}_detailed": detailed_icon(kind) for kind in M} | {
    "file_other_detailed": lambda: Icon().back(body()).mark(fold())
}
