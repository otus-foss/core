"""Content, formatting, security and system symbols."""

import math

from glyph import Icon, Shape, arc, circle, pill, poly, rbox, stroke, union


def below(y0):
    """Everything below the line y = y0."""
    return Shape(lambda x, y: y0 - y)


def above(y0):
    """Everything above the line y = y0."""
    return Shape(lambda x, y: y - y0)


def ray(cx, cy, deg, inner, outer, width=2.8):
    a = math.radians(deg)
    return stroke(
        [
            (cx + inner * math.cos(a), cy + inner * math.sin(a)),
            (cx + outer * math.cos(a), cy + outer * math.sin(a)),
        ],
        width,
    )


def slash():
    return stroke([(4, 4), (20, 20)], 3.2)


def visibility_off():
    lens = (circle(12, 15.6, 10.6) & circle(12, 8.4, 10.6)).round(2)
    eye = lens - circle(12, 12, 3.4)
    return Icon().back(eye).front(stroke([(3.5, 5.6), (18.4, 20.5)], 3.2))


def key():
    """One piece: a round bow with a hole, a shaft, and two teeth."""
    bow = circle(7.4, 12, 5.4) - circle(7.4, 12, 2)
    blade = union(
        stroke([(11, 12), (20.6, 12)], 3.6),
        stroke([(16, 12), (16, 15.6)], 3.2),
        stroke([(20, 12), (20, 15)], 3.2),
    )
    return Icon().front((bow | blade).round(0.8))


def lock():
    shackle = stroke([(7.6, 12)] + arc(12, 8, 4.4, 180, 360) + [(16.4, 12)], 3.2)
    body = rbox(3.5, 10, 17, 12, 4.5) - circle(12, 15.8, 1.9)
    return Icon().back(shackle).front(body)


def shield_shape():
    return (circle(23, 4, 19) & circle(1, 4, 19) & below(2.5)).round(4)


def shield():
    return Icon().front(shield_shape())


def verified_user():
    body = shield_shape()
    check = stroke([(8.4, 12.4), (11, 15), (15.8, 10.2)], 3)
    return Icon().back(body).mark(check.center_in(body))


def link():
    def chain_ring(x):
        return (rbox(x, 7.5, 11, 9, 4.5) - rbox(x + 3, 10.5, 5, 3, 1.5)).rotate(-45)

    return Icon().back(chain_ring(2.4)).front(chain_ring(10.6))


def content_copy():
    return Icon().back(rbox(3, 2, 12.5, 15, 4)).front(rbox(8.5, 7, 12.5, 15, 4))


def arrow_down(top, tip):
    shaft = stroke([(12, top), (12, tip - 4)], 4)
    head = poly([(6.2, tip - 6.4), (17.8, tip - 6.4), (12, tip)]).round(1.4)
    return (shaft | head).round(0.8)


def tray():
    return pill(3.5, 18, 17, 3.6)


def download():
    return Icon().back(tray()).front(arrow_down(3, 18))


def upload():
    return Icon().back(tray()).front(arrow_down(3, 16).flip(origin=(12, 9.5)))


def print_():
    return (
        Icon()
        .back(pill(6.5, 2.5, 11, 3.6))
        .back(rbox(2.5, 7.6, 19, 10.6, 3.6))
        .front(rbox(6.5, 13, 11, 9, 2.2))
    )


def call_merge():
    stems = union(
        stroke([(6, 20.5), (6, 18.5), (12, 13)], 3.4),
        stroke([(18, 20.5), (18, 18.5), (12, 13)], 3.4),
        stroke([(12, 13.5), (12, 8)], 3.4),
    )
    head = poly([(6.6, 9.6), (17.4, 9.6), (12, 3)]).round(1.4)
    return Icon().front((stems | head).round(0.8))


def grid_on():
    return Icon().front(
        union(
            *[rbox(2.8 + 6.6 * i, 2.8 + 6.6 * j, 5.2, 5.2, 1.8) for i in range(3) for j in range(3)]
        )
    )


def view_agenda():
    return Icon().front(rbox(3, 3, 18, 8, 3.5) | rbox(3, 13, 18, 8, 3.5))


def short_text():
    return Icon().front(stroke([(4, 9), (20, 9)], 3.4) | stroke([(4, 15), (14, 15)], 3.4))


def notes():
    return Icon().front(
        union(
            stroke([(4, 6.5), (20, 6.5)], 3.4),
            stroke([(4, 12), (20, 12)], 3.4),
            stroke([(4, 17.5), (13, 17.5)], 3.4),
        )
    )


def format_bold():
    """A stem and two bowls drawn as one fat stroke, so every curve shares one radius."""
    width, left, top, mid, bottom = 3.6, 7, 4.8, 12, 19.2
    upper = [(left, top), (11.6, top)] + arc(11.6, (top + mid) / 2, (mid - top) / 2, -90, 90)
    lower = [(left, mid), (12.6, mid)] + arc(12.6, (mid + bottom) / 2, (bottom - mid) / 2, -90, 90)
    return Icon().front(
        union(
            stroke([(left, top), (left, bottom)], width),
            stroke(upper + [(left, mid)], width),
            stroke(lower + [(left, bottom)], width),
        )
    )


def format_italic():
    return Icon().front(
        union(
            stroke([(10, 4.5), (18.5, 4.5)], 3.2),
            stroke([(5.5, 19.5), (14, 19.5)], 3.2),
            stroke([(14.5, 4.5), (9.5, 19.5)], 3.4),
        ).round(0.8)
    )


def format_clear():
    """An eraser: light rubber end, dark body."""

    def place(shape):
        return shape.rotate(-45, origin=(0, 0)).move(12, 12)

    block = place(rbox(-9.4, -4.6, 18.8, 9.2, 3.6))
    body = place(rbox(-2.6, -4.6, 12, 9.2, 3.6))
    return Icon().back(block).front(body)


def format_list_bulleted():
    rows = (5.5, 12, 18.5)
    return Icon().front(
        union(
            *[circle(5, y, 2.2) for y in rows], *[stroke([(10.5, y), (20.5, y)], 3.2) for y in rows]
        )
    )


def format_list_numbered():
    one = stroke([(3, 4.6), (5.2, 3.4), (5.2, 10.2)], 2.6)
    two = stroke(
        [(2.9, 15.2)] + arc(5.1, 15.6, 2.1, -160, 10)[1:] + [(3.1, 20.6), (7.2, 20.6)], 2.6
    )
    rows = [stroke([(10.5, y), (20.5, y)], 3.4) for y in (6.7, 17.3)]
    return Icon().front(union(one, two, *rows))


def code():
    left = stroke([(8.5, 6), (3.2, 12), (8.5, 18)], 3.4)
    return Icon().front(left | left.mirror())


def terminal():
    window = rbox(2.5, 3.5, 19, 17, 4.5)
    prompt = stroke([(7, 9), (10.4, 12), (7, 15)], 3)
    underline = stroke([(12.6, 15.3), (17, 15.3)], 3)
    return Icon().back(window).mark(prompt | underline)


def cloud_shape():
    left, middle, right = circle(7, 15.2, 4.8), circle(12.6, 10.8, 6.2), circle(17.6, 15.6, 4.4)
    return union(left, middle, right, rbox(7, 12, 10.6, 8, 0)).round(1.6)


def cloud():
    return Icon().front(cloud_shape())


def cloud_off():
    return Icon().back(cloud_shape()).front(slash())


def public():
    globe = circle(12, 12, 10)
    lands = union(
        circle(7, 8.4, 3).blend(circle(8.6, 12, 2.2), 1.6),
        circle(15.6, 6.2, 2.4).blend(circle(17.8, 8.8, 2), 1.4),
        circle(7.4, 17.8, 2),
        circle(16.6, 16, 2.8).blend(circle(14.6, 18.6, 1.8), 1.4),
    )
    return Icon().back(globe).mark((lands & globe.grow(-0.1)).round(1))


def home():
    house = poly([(12, 2.6), (21, 10.4), (21, 21), (3, 21), (3, 10.4)]).round(3)
    door = rbox(9.5, 14, 5, 7, 2.5) | rbox(9.5, 18, 5, 3, 0)
    return Icon().back(house).mark(door)


def work():
    case = rbox(2.5, 8.5, 19, 12.5, 4.5)
    handle = stroke([(8.8, 10), (8.8, 4.2), (15.2, 4.2), (15.2, 10)], 3).round(1)
    return Icon().back(handle).front(case)


def text_fields():
    """A large T in front of a small one, the two sizes of type."""
    large = stroke([(3.5, 5), (14.5, 5)], 3.6) | stroke([(9, 5), (9, 19.5)], 3.8)
    small = stroke([(13.5, 11), (20.5, 11)], 3.2) | stroke([(17, 11), (17, 19.5)], 3.4)
    return Icon().back(small).front(large)


def speed():
    dial = (circle(12, 14, 10) & above(20.5)).round(2)
    needle = stroke([(12, 15.5), (16.4, 9.2)], 3) | circle(12, 15.5, 2.7)
    return Icon().back(dial).mark(needle)


def palette():
    board = (circle(12, 12, 10) - circle(16.8, 16.8, 3.3)).round(1)
    dots = union(
        *[circle(x, y, 1.9) for x, y in ((6.8, 11.5), (9, 6.8), (14.6, 6.4), (17.4, 10.6))]
    )
    return Icon().back(board).mark(dots)


def brush():
    tip = (circle(8, 16, 4.4) | poly([(4, 17), (8, 20.4), (2.6, 21.6)])).round(1.8)
    return Icon().back(tip).front(stroke([(20, 4), (12, 12)], 3.8))


def light_mode():
    rays = union(*[ray(12, 12, a, 7.7, 9.6) for a in range(0, 360, 45)])
    return Icon().back(rays).front(circle(12, 12, 4.8))


def nightlight():
    return Icon().front((circle(12, 12, 9.5) - circle(17.5, 7.5, 7.8)).round(1))


def wb_twilight():
    rays = union(*[ray(12, 16.5, a, 9.3, 11.4) for a in (-90, -135, -45)])
    sun = (circle(12, 16.5, 6.4) & above(16.5)).round(1)
    return Icon().back(rays).front(sun | stroke([(3.5, 20), (20.5, 20)], 3))


ICONS = {
    "key": key,
    "visibility_off": visibility_off,
    "lock": lock,
    "shield": shield,
    "verified_user": verified_user,
    "link": link,
    "content_copy": content_copy,
    "download": download,
    "upload": upload,
    "print": print_,
    "call_merge": call_merge,
    "grid_on": grid_on,
    "view_agenda": view_agenda,
    "short_text": short_text,
    "notes": notes,
    "format_bold": format_bold,
    "format_clear": format_clear,
    "format_italic": format_italic,
    "format_list_bulleted": format_list_bulleted,
    "format_list_numbered": format_list_numbered,
    "code": code,
    "terminal": terminal,
    "cloud": cloud,
    "cloud_off": cloud_off,
    "public": public,
    "home": home,
    "work": work,
    "speed": speed,
    "text_fields": text_fields,
    "palette": palette,
    "brush": brush,
    "light_mode": light_mode,
    "nightlight": nightlight,
    "wb_twilight": wb_twilight,
}
