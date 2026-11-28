"""File-type symbols: the draft symbol's page, without its fold, in the light tone, with the type
as a solid mark in the middle of the page. Every mark is drawn in one 10 x 9.6 box (x 7 to 17,
y 10.2 to 19.8) with 2.4-unit strokes, then lifted into the page's visual middle, which sits
lower than its true middle because the cut corner takes weight off the top."""

from glyph import Icon, arc, circle, pill, poly, rbox, ring, stroke, union

# How far each mark moves up from the box it is drawn in.
LIFT = 1.6


def page():
    """The draft symbol's page: its top end corner cut off where the fold would go."""
    return (rbox(4, 2.5, 16, 19, 4.5) - poly([(10.5, 0), (24, 0), (24, 13.5)])).round(1)


W = 2.4


def line(y, length=10):
    return pill(7, y - W / 2, length, W)


MARKS = {
    "text": lambda: line(11.6) | line(14.8) | line(18, 6),
    "document": lambda: rbox(7, 10.2, 10, 3.6, 1.5) | line(15.6) | line(18.6, 6),
    "sheet": lambda: union(
        *[rbox(x, y, 4.3, 2.6, 1) for x in (7, 12.7) for y in (10.2, 13.8, 17.4)]
    ),
    "slides": lambda: union(
        *[pill(x - W / 2, 19.6 - h, W, h) for x, h in ((8.4, 5), (12, 8.6), (15.6, 6.4))]
    ),
    "image": lambda: (
        (
            poly([(6.6, 19.6), (10.8, 12.2), (15, 19.6)]).round(1)
            | poly([(11, 19.6), (14.2, 14.6), (17.4, 19.6)]).round(1)
        )
        | circle(15.2, 11.4, 1.8)
    ),
    "video": lambda: poly([(8.8, 10.4), (16.4, 15), (8.8, 19.6)]).round(1.6),
    "audio": lambda: (
        circle(9.9, 17.2, 2.5) | stroke([(12.2, 17.2), (12.2, 10.8), (16, 12.2)], W)
    ).round(0.4),
    "archive": lambda: (
        pill(6.8, 10.4, 10.4, 3) | (rbox(7.6, 14.6, 8.8, 5.2, 1.8) - pill(10.2, 16.2, 3.6, 1.9))
    ),
    "calendar": lambda: (
        (rbox(7, 11.2, 10, 8.6, 2.4) | pill(8.9, 9.6, 2.2, 3.4) | pill(12.9, 9.6, 2.2, 3.4))
        - rbox(12, 15.2, 3.4, 3, 1)
    ),
    "contact": lambda: (
        circle(12, 12.8, 2.9) | (circle(12, 22.4, 6.6) & rbox(4, 16.8, 16, 3, 0)).round(0.8)
    ),
    "email": lambda: (
        rbox(6.8, 11, 10.4, 8.4, 2.2) - stroke([(8.8, 13.6), (12, 16.2), (15.2, 13.6)], 2)
    ),
    "code": lambda: (
        stroke([(10.2, 11.4), (7.2, 15), (10.2, 18.6)], W)
        | stroke([(13.8, 11.4), (16.8, 15), (13.8, 18.6)], W)
    ),
    "key": lambda: (
        ring(10, 13.2, 2.9, W)
        | stroke([(12, 15.2), (16, 19.2)], W)
        | stroke([(14.4, 17.6), (16.2, 15.8)], 2.2)
    ),
    "signature": lambda: (
        (circle(12, 13.6, 3.8) - circle(12, 13.6, 1.4))
        | stroke([(10.2, 16.6), (9.4, 19.4)], 2.2)
        | stroke([(13.8, 16.6), (14.6, 19.4)], 2.2)
    ),
    "pdf": lambda: rbox(6.6, 12.4, 10.8, 5.2, 2.6),
    "data": lambda: (
        stroke([(10.2, 10.8), (9, 11.8), (9, 14), (7.4, 15), (9, 16), (9, 18.2), (10.2, 19.2)], 2.2)
        | stroke(
            [(13.8, 10.8), (15, 11.8), (15, 14), (16.6, 15), (15, 16), (15, 18.2), (13.8, 19.2)],
            2.2,
        )
    ),
    "design": lambda: (
        stroke(arc(16.4, 19, 8, 180, 270), 2.2)
        | rbox(6.6, 16.8, 3.4, 3.4, 1)
        | rbox(14.4, 9.6, 3.4, 3.4, 1)
    ),
    "disk_image": lambda: circle(12, 15, 4.9) - circle(12, 15, 1.5),
    "web": lambda: (
        ring(12, 15, 4.8, 2)
        | stroke([(7.6, 15), (16.4, 15)], 2)
        | stroke(arc(17.4, 15, 6.2, 141, 219), 2)
        | stroke(arc(6.6, 15, 6.2, -39, 39), 2)
    ),
    "encrypted": lambda: (
        (
            stroke(
                [(9.4, 14.4), (9.4, 12.8)] + arc(12, 12.8, 2.6, 180, 360)[1:] + [(14.6, 14.4)], 2.2
            )
            | rbox(7.4, 14, 9.2, 6, 2)
        )
        - circle(12, 16.8, 1.1)
    ),
    "font": lambda: (
        stroke([(8, 19.4), (12, 10.6), (16, 19.4)], W) | stroke([(9.8, 16.4), (14.2, 16.4)], 2.2)
    ),
    "ebook": lambda: (
        poly([(6.8, 11), (11.3, 12), (11.3, 19.8), (6.8, 18.8)]).round(1)
        | poly([(17.2, 11), (12.7, 12), (12.7, 19.8), (17.2, 18.8)]).round(1)
    ),
    "location": lambda: (
        (circle(12, 13.8, 3.9) | poly([(8.7, 15.9), (15.3, 15.9), (12, 20)]).round(0.8))
        - circle(12, 13.8, 1.4)
    ),
    "database": lambda: union(*[rbox(7.4, y, 9.2, 2.6, 1.3) for y in (10.4, 13.9, 17.4)]),
    "app": lambda: union(*[rbox(x, y, 4.3, 4.3, 1.5) for x in (7, 12.7) for y in (10.2, 15.5)]),
}


def file_icon(kind):
    return lambda: Icon().back(page()).mark(MARKS[kind]().move(0, -LIFT))


ICONS = {f"file_{kind}": file_icon(kind) for kind in MARKS} | {
    "file_other": lambda: Icon().back(page())
}
