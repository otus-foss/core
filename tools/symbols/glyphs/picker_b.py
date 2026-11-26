"""Folder-picker symbols, second half of the alphabet."""

import math

import numpy as np
from glyph import Icon, Shape, arc, circle, pill, poly, rbox, ring, stroke, union


def ellipse(cx, cy, rx, ry):
    return Shape(lambda x, y: (np.hypot((x - cx) / rx, (y - cy) / ry) - 1) * min(rx, ry))


def below(y0):
    return Shape(lambda x, y: y0 - y)


def vesica(cx, top, bottom, width):
    """A pointed petal from top to bottom, made of two circles."""
    half_h, half_w = (bottom - top) / 2, width / 2
    r = (half_h**2 + half_w**2) / (2 * half_w)
    cy = (top + bottom) / 2
    return circle(cx - (r - half_w), cy, r) & circle(cx + (r - half_w), cy, r)


def wave(y, x0=3.5, x1=20.5, amp=1.3, period=8.5, steps=40):
    return [
        (
            x0 + (x1 - x0) * i / steps,
            y + amp * math.sin(2 * math.pi * (x1 - x0) * i / steps / period),
        )
        for i in range(steps + 1)
    ]


def menu_book():
    left = poly([(2.5, 4.5), (11.25, 6.5), (11.25, 20.5), (2.5, 18.5)]).round(2.2)
    return Icon().front(left | left.mirror())


def movie():
    body = rbox(3, 10, 18, 11, 4.5)
    clapper = pill(3, 4.6, 18, 4.6).rotate(10, origin=(3, 9.2))
    return Icon().back(clapper).front(body)


def music_note():
    head = ellipse(8.2, 17.2, 4.4, 3.8)
    stem = stroke([(11.2, 17), (11.2, 4)], 3.2)
    flag = stroke([(11.2, 4), (17.5, 6), (17.5, 9.5)], 3.2)
    return Icon().front(union(head, stem, flag).round(0.8))


def newspaper():
    page = rbox(3, 3, 18, 18, 4.5)
    marks = union(
        pill(6.5, 6.5, 11, 3),
        rbox(6.5, 11.5, 5, 6, 1.6),
        pill(13.5, 11.5, 4, 2.4),
        pill(13.5, 15.1, 4, 2.4),
    )
    return Icon().back(page).mark(marks)


def pets():
    pad = ellipse(12, 16.2, 5.6, 4.6)
    toes = union(
        ellipse(4.8, 11, 2.3, 2.8).rotate(20, origin=(4.8, 11)),
        ellipse(9, 6, 2.3, 2.9).rotate(8, origin=(9, 6)),
        ellipse(15, 6, 2.3, 2.9).rotate(-8, origin=(15, 6)),
        ellipse(19.2, 11, 2.3, 2.8).rotate(-20, origin=(19.2, 11)),
    )
    return Icon().front(pad | toes)


def piano():
    body = rbox(3, 3, 18, 18, 4.5)
    keys = union(*[rbox(x - 1.7, 1, 3.4, 11.5, 1.7) for x in (7.5, 12, 16.5)]) & body
    dividers = union(*[rbox(x - 0.75, 12, 1.5, 10) for x in (7.5, 12, 16.5)])
    return Icon().back((body - dividers).round(0.6)).mark(keys)


def pool():
    waves = stroke(wave(16.2), 3) | stroke(wave(20.6), 3)
    rails = stroke([(8.5, 3.5), (8.5, 15)], 2.8) | stroke([(15.5, 3.5), (15.5, 15)], 2.8)
    rungs = union(*[stroke([(8.5, y), (15.5, y)], 2.4) for y in (7.2, 11)])
    return Icon().back(rails | rungs).front(waves)


def psychology():
    head = union(
        circle(11, 10, 7.5),
        rbox(5.5, 12, 7.5, 9.5, 2),
        poly([(15.5, 7.5), (20.5, 13.5), (17.6, 14.2), (17.6, 17.4), (13, 17.4)]),
    ).round(1.4)
    teeth = union(
        *[rbox(12.7, 8.35, 2.2, 1.9, 0.7).rotate(a, origin=(10.5, 9.3)) for a in range(0, 360, 60)]
    )
    hole = circle(10.5, 9.3, 1.0)
    gear = (circle(10.5, 9.3, 2.7) | teeth).round(0.4) - hole
    return Icon().back(head - hole).mark(gear)


def restaurant():
    spoon = ellipse(7.5, 8, 3.6, 5) | stroke([(7.5, 11), (7.5, 20.5)], 3)
    blade = poly([(15, 14.5), (15, 6.5), (17.6, 3), (20, 4.2), (20, 14.5)]).round(1.2)
    knife = blade | stroke([(17.5, 14), (17.5, 20.5)], 3)
    return Icon().front((spoon | knife).round(0.8))


def rocket_launch():
    body = union(
        rbox(-3.6, -5, 7.2, 12, 2.5),
        poly([(-3.6, -3), (0, -10.5), (3.6, -3)]),
        poly([(-3.6, 0), (-7.4, 7.5), (-3.6, 7)]),
        poly([(3.6, 0), (7.4, 7.5), (3.6, 7)]),
    ).move(12, 11)
    body = body.round(1.2) - circle(12, 8.5, 1.7)
    flame = circle(12, 22.2, 2.8) | poly([(9.6, 21.2), (12, 17.2), (14.4, 21.2)])
    return Icon().back(flame.round(0.8).rotate(45)).front(body.rotate(45))


def sailing():
    hull = poly([(2.5, 16), (21.5, 16), (18, 21), (6, 21)]).round(1.5)
    sails = poly([(11, 2), (11, 14.5), (3, 14.5)]).round(1.6) | poly(
        [(12.8, 4.5), (12.8, 14.5), (20, 14.5)]
    ).round(1.6)
    return Icon().back(sails).front(hull)


def savings():
    body = union(
        ellipse(11.5, 14, 8, 6),
        rbox(17.5, 11.2, 4, 4.4, 1.6),
        pill(6.5, 16, 3.4, 5.5),
        pill(13.5, 16, 3.4, 5.5),
        poly([(7, 10), (6.5, 5.5), (11, 8.5)]),
    ).round(1.0)
    return Icon().back(body).mark(circle(15.5, 12, 1.2)).front(circle(13, 4.6, 2.8))


def school():
    cap = poly([(12, 3), (22, 8.5), (12, 14), (2, 8.5)]).round(1.5)
    tassel = stroke([(19.6, 9.5), (19.6, 15.5)], 2.4)
    base = rbox(6.5, 8, 11, 11.5, 4.5)
    return Icon().back(base).front(cap | tassel)


def science():
    neck = rbox(9.6, 3, 4.8, 8, 0)
    bulb = poly([(9.6, 9), (14.4, 9), (20.5, 19.5), (19, 21), (5, 21), (3.5, 19.5)])
    flask = (neck | bulb).round(1.6) | pill(8, 2.5, 8, 3.2)
    liquid = (flask & below(13.6)).round(1.2)
    return Icon().back(flask).front(liquid)


def self_improvement():
    head = circle(12, 5, 3)
    arms = stroke([(3.5, 13), (8.5, 10.5), (15.5, 10.5), (20.5, 13)], 3)
    torso = rbox(9, 9, 6, 9, 2.8)
    legs = pill(3, 16.5, 18, 4.5)
    return Icon().front(union(head, (arms | torso | legs).round(1)))


def shopping_bag():
    body = rbox(3.5, 8.5, 17, 13, 4.5)
    handle = stroke(arc(12, 8, 4.6, 180, 360, steps=24), 2.8)
    return Icon().back(handle).front(body)


def spa():
    center = vesica(12, 3, 19.5, 6.4)
    side = vesica(12, 6, 20, 5.4)
    left, right = (side.rotate(a, origin=(12, 20)).round(0.8) for a in (52, -52))
    return Icon().back(left).back(right).front(center.round(0.8))


def sports_basketball():
    ball = circle(12, 12, 10)
    seams = union(
        rbox(11.25, 0, 1.5, 24),
        rbox(0, 11.25, 24, 1.5),
        ring(-1.5, 12, 10.5, 1.5),
        ring(25.5, 12, 10.5, 1.5),
    )
    return Icon().front((ball - seams).round(0.6))


def sports_esports():
    body = union(rbox(2, 6, 20, 10, 5), circle(6, 15.5, 4), circle(18, 15.5, 4)).round(1.5)
    plus = pill(5.2, 10.6, 5.6, 2) | pill(7, 8.8, 2, 5.6)
    buttons = circle(15.2, 12.6, 1.5) | circle(17.8, 9.8, 1.5)
    return Icon().back(body).mark(plus | buttons)


def sports_soccer():
    ball = circle(12, 12, 10)
    pentagon = poly(
        [
            (
                12 + 3.8 * math.cos(math.radians(-90 + 72 * k)),
                12 + 3.8 * math.sin(math.radians(-90 + 72 * k)),
            )
            for k in range(5)
        ]
    ).round(0.9)
    patches = union(
        *[
            circle(
                12 + 10.5 * math.cos(math.radians(-90 + 72 * k)),
                12 + 10.5 * math.sin(math.radians(-90 + 72 * k)),
                3.4,
            )
            & ball
            for k in range(5)
        ]
    ).round(0.6)
    return Icon().back(ball).mark(pentagon | patches)


def storefront():
    awning = union(
        pill(2.5, 3, 19, 4.6), *[circle(x, 7.4, 2.375) for x in (4.875, 9.625, 14.375, 19.125)]
    ).round(0.6)
    building = rbox(4, 7, 16, 14, 4.5)
    door = rbox(9.5, 14, 5, 8, 2) & building
    return Icon().back(building).mark(door).front(awning)


def straighten():
    body = rbox(2, 7, 20, 10, 3)
    ticks = union(
        *[pill(x - 0.9, 5, 1.8, h + 2) for x, h in ((6, 3.6), (9.3, 5.4), (12.6, 3.6), (15.9, 5.4))]
    )
    return Icon().front((body - ticks).round(0.5))


def train():
    body = rbox(4, 2.5, 16, 15, 5)
    window = rbox(6.8, 5.5, 10.4, 5.2, 1.8)
    lights = circle(8.4, 14, 1.4) | circle(15.6, 14, 1.4)
    legs = stroke([(8.6, 19.8), (7, 21.6)], 2.8) | stroke([(15.4, 19.8), (17, 21.6)], 2.8)
    return Icon().back(body).mark(window | lights).front(legs)


def volunteer_activism():
    heart = union(
        circle(11.9, 7.2, 3),
        circle(16.1, 7.2, 3),
        poly([(9.15, 8.4), (18.85, 8.4), (14, 13.9)]),
    ).round(0.9)
    hand = (
        rbox(2.5, 12.5, 3.4, 9, 1.7)
        | poly([(6.5, 13), (10, 13), (14.5, 15.8), (20.5, 15.8), (20.5, 18), (15, 21), (6.5, 21)])
    ).round(1.4)
    return Icon().back(hand).front(heart)


def water_drop():
    cx, cy, r, tip = 12, 14.6, 7, 2.2
    angle = math.asin(r / (cy - tip))
    reach = math.hypot(cy - tip, 0) * math.cos(angle)
    side = [(cx + s * reach * math.sin(angle), tip + reach * math.cos(angle)) for s in (-1, 1)]
    cone = poly([(cx, tip), side[1], (cx, cy), side[0]])
    return Icon().front((circle(cx, cy, r) | cone).round(1.2))


def workspace_premium():
    medal = circle(12, 8.5, 6.3) - circle(12, 8.5, 2.6)
    ribbon = poly([(7.5, 11), (16.5, 11), (16.5, 22), (12, 19.2), (7.5, 22)]).round(1.2)
    return Icon().back(ribbon).front(medal)


ICONS = {
    f.__name__: f
    for f in (
        menu_book,
        movie,
        music_note,
        newspaper,
        pets,
        piano,
        pool,
        psychology,
        restaurant,
        rocket_launch,
        sailing,
        savings,
        school,
        science,
        self_improvement,
        shopping_bag,
        spa,
        sports_basketball,
        sports_esports,
        sports_soccer,
        storefront,
        straighten,
        train,
        volunteer_activism,
        water_drop,
        workspace_premium,
    )
}
