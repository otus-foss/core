"""Generic controls, arrows and plain shapes."""

import math

from glyph import Icon, arc, circle, poly, rbox, ring, stroke, union

WIDTH = 3.4
HEAD = 4.2


def strokes(*paths, width=WIDTH, fillet=0.6):
    """Joined strokes with their inside corners filleted."""
    shape = union(*[stroke(p, width) for p in paths])
    return shape.round(fillet) if fillet else shape


def arrow(tip, tail, head=HEAD, width=WIDTH):
    """A straight arrow from tail to tip with an open chevron head."""
    (tx, ty), (sx, sy) = tip, tail
    length = math.hypot(tx - sx, ty - sy)
    ux, uy = (sx - tx) / length, (sy - ty) / length
    wings = [
        (
            tx + head * math.sqrt(2) * (ux * math.cos(a) - uy * math.sin(a)),
            ty + head * math.sqrt(2) * (ux * math.sin(a) + uy * math.cos(a)),
        )
        for a in (math.radians(45), math.radians(-45))
    ]
    return strokes([tail, tip], [wings[0], tip, wings[1]], width=width, fillet=0)


def regular(sides, r, rounding, start=-90):
    points = [
        (
            12 + r * math.cos(math.radians(start + 360 * i / sides)),
            12 + r * math.sin(math.radians(start + 360 * i / sides)),
        )
        for i in range(sides)
    ]
    return poly(points).round(rounding)


def disc():
    return circle(12, 12, 10)


def one(shape):
    return Icon().front(shape)


def more_horiz():
    return one(union(*[circle(x, 12, 2.6) for x in (5, 12, 19)]))


def add():
    return one(strokes([(4.5, 12), (19.5, 12)], [(12, 4.5), (12, 19.5)]))


def remove():
    return one(strokes([(4.5, 12), (19.5, 12)]))


def check():
    return one(strokes([(4.5, 12.5), (9.4, 17.4), (19.5, 7.2)]))


def close():
    return one(strokes([(6, 6), (18, 18)], [(18, 6), (6, 18)]))


def chevron_left():
    return one(strokes([(14.5, 5), (7.5, 12), (14.5, 19)]))


def chevron_right():
    return one(strokes([(9.5, 5), (16.5, 12), (9.5, 19)]))


def expand_more():
    return one(strokes([(5, 9), (12, 16), (19, 9)]))


def expand_less():
    return one(strokes([(5, 15), (12, 8), (19, 15)]))


def unfold_more():
    return one(strokes([(7, 9), (12, 4), (17, 9)], [(7, 15), (12, 20), (17, 15)]))


def unfold_less():
    return one(strokes([(7, 4), (12, 9), (17, 4)], [(7, 20), (12, 15), (17, 20)]))


def arrow_upward():
    return one(arrow((12, 4.5), (12, 19.5)))


def arrow_outward():
    return one(arrow((17.5, 6.5), (6.5, 17.5)))


def menu():
    return one(strokes(*[[(4.5, y), (19.5, y)] for y in (6, 12, 18)]))


def drag_indicator():
    return one(union(*[circle(x, y, 2.1) for x in (9, 15) for y in (5.5, 12, 18.5)]))


def swap_pair():
    top = arrow((19.5, 6.8), (6, 6.8), 3.6)
    bottom = arrow((4.5, 17.2), (18, 17.2), 3.6)
    return top | bottom


def swap_horiz():
    return one(swap_pair())


def swap_vert():
    return one(swap_pair().rotate(90))


def swipe_left():
    return Icon().back(circle(16.8, 12, 5.2)).front(arrow((2.6, 12), (9.9, 12), 4.4))


def swipe_right():
    return (
        Icon().back(circle(16.8, 12, 5.2).mirror()).front(arrow((2.6, 12), (9.9, 12), 4.4).mirror())
    )


def circle_fill():
    return one(circle(12, 12, 9.5))


def radio_button_unchecked():
    return one(ring(12, 12, 9.5 - WIDTH / 2, WIDTH))


def square_fill():
    return one(rbox(3.5, 3.5, 17, 17, 5))


def hexagon_fill():
    return one(regular(6, 10.2, 2.6))


def pentagon_fill():
    return one(regular(5, 10.3, 2.6))


def change_history_fill():
    return one(regular(3, 12.6, 3.0, start=-90).move(0, 1.6))


def check_circle():
    tick = strokes([(7.4, 12.4), (10.6, 15.6), (16.6, 9.4)], width=3.2)
    return Icon().back(disc()).mark(tick.center_in(disc()))


def info():
    mark = rbox(10.4, 10.2, 3.2, 7.6, 1.6) | circle(12, 7.2, 1.8)
    return Icon().back(disc()).mark(mark.center_in(disc()))


def help():  # noqa: A001 - named after the Material Symbol it replaces
    hook = stroke(arc(12, 9.6, 3.4, 180, 400) + [(12, 13.6), (12, 14)], 3.0)
    mark = hook | circle(12, 17.6, 1.8)
    return Icon().back(disc()).mark(mark.center_in(disc()))


ICONS = {
    "more_horiz": more_horiz,
    "add": add,
    "remove": remove,
    "check": check,
    "check_circle": check_circle,
    "check_circle_fill": check_circle,
    "close": close,
    "chevron_left": chevron_left,
    "chevron_right": chevron_right,
    "expand_less": expand_less,
    "expand_more": expand_more,
    "unfold_less": unfold_less,
    "unfold_more": unfold_more,
    "arrow_upward": arrow_upward,
    "arrow_outward": arrow_outward,
    "menu": menu,
    "drag_indicator": drag_indicator,
    "swap_horiz": swap_horiz,
    "swap_vert": swap_vert,
    "swipe_left": swipe_left,
    "swipe_right": swipe_right,
    "radio_button_unchecked": radio_button_unchecked,
    "circle_fill": circle_fill,
    "square_fill": square_fill,
    "hexagon_fill": hexagon_fill,
    "pentagon_fill": pentagon_fill,
    "change_history_fill": change_history_fill,
    "info": info,
    "help": help,
}
