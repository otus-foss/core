"""Folder-picker symbols, first half: ac_unit to medical_services."""

import math

from glyph import Icon, Shape, arc, circle, pill, poly, rbox, ring, stroke, union


def polar(r, deg, cx=12, cy=12):
    return cx + r * math.sin(math.radians(deg)), cy - r * math.cos(math.radians(deg))


def below(y0):
    return Shape(lambda x, y: y0 - y)


def above(y0):
    return Shape(lambda x, y: y - y0)


def ac_unit():
    arms = [stroke([polar(9, a), polar(9, a + 180)], 3) for a in (0, 60, 120)]
    hooks = [
        stroke([polar(8.4, a - 20), polar(6.2, a), polar(8.4, a + 20)], 2.6)
        for a in range(0, 360, 60)
    ]
    return Icon().front(union(*arms, *hooks))


def account_balance():
    roof = poly([(2.5, 9), (12, 2.5), (21.5, 9)]).round(1.4)
    base = pill(2.5, 18.5, 19, 3.5)
    pillars = union(*[rbox(x, 7, 3.4, 14, 1.2) for x in (4.6, 10.3, 16)])
    return Icon().back(pillars).front(roof).front(base)


def anchor():
    shank = stroke([(12, 7), (12, 20)], 3)
    bar = stroke([(7.8, 10.5), (16.2, 10.5)], 3)
    bow = stroke(arc(12, 11.5, 8.4, 25, 155), 3)
    return Icon().front(union(ring(12, 5, 2.6, 2.8), shank, bar, bow))


def beach_access():
    canopy = (circle(12, 11.5, 9.5) & above(11.5)).round(1.6)
    pole = stroke([(12, 9.5), (12, 18.4)] + arc(9.6, 18.4, 2.4, 0, 180)[1:], 3)
    return Icon().back(canopy).front(pole)


def bolt():
    points = [(14.2, 2), (5, 13.6), (11.4, 13.6), (9.8, 22), (19, 10.4), (12.6, 10.4)]
    return Icon().front(poly(points).scale(1.2).round(1.2))


def build():
    head = circle(16, 8, 5.6)
    jaw = rbox(14.4, -1.5, 3.2, 9.5, 1.6).rotate(45, origin=(16, 8))
    handle = stroke([(5.6, 18.4), (13, 11)], 4.4)
    return Icon().front(((head - jaw) | handle).round(0.8))


def cake():
    bottom = rbox(3, 13, 18, 8.5, 3.6)
    top = rbox(6, 6.5, 12, 8, 3)
    candle = pill(10.4, 1.8, 3.2, 4.8)
    return Icon().back(top).front(bottom).front(candle)


def castle():
    walls = union(
        rbox(3, 8, 18, 13, 2.5),
        rbox(3, 3.5, 4.6, 6.5, 1.6),
        rbox(9.7, 3.5, 4.6, 6.5, 1.6),
        rbox(16.4, 3.5, 4.6, 6.5, 1.6),
    ).round(0.8)
    door = rbox(9.5, 14, 5, 9, 2.5) & walls
    return Icon().back(walls).mark(door)


def cruelty_free():
    head = circle(12, 15.4, 6.2)
    ear = pill(7.8, 1.6, 4.6, 12).rotate(-18, origin=(10.1, 13))
    ears = ear | ear.mirror()
    eyes = circle(9.6, 15, 1.2) | circle(14.4, 15, 1.2)
    return Icon().back(ears).front(head - eyes)


def diamond():
    crown = poly([(7.2, 3.5), (16.8, 3.5), (21.5, 9.4), (2.5, 9.4)]).round(1.4)
    pavilion = poly([(2.5, 10.8), (21.5, 10.8), (12, 21.5)]).round(1.6)
    return Icon().back(crown).front(pavilion)


def directions_bike():
    wheels = ring(5.8, 16, 3.4, 2.8) | ring(18.2, 16, 3.4, 2.8)
    frame = stroke([(5.8, 16), (10, 9.4), (15.4, 9.4), (18.2, 16)], 2.8)
    seat = stroke([(10, 9.4), (11.6, 15.6), (15.4, 9.4)], 2.8)
    bars = stroke([(14.2, 5.8), (16.6, 5.8), (15.4, 9.4)], 2.8)
    return Icon().front(union(wheels, frame, seat, bars))


def directions_car():
    cabin = poly([(5.6, 11), (7.6, 4.5), (16.4, 4.5), (18.4, 11)]).round(1.6)
    body = union(rbox(2.5, 10, 19, 8, 3), pill(4.4, 15, 4.4, 5.6), pill(15.2, 15, 4.4, 5.6))
    return Icon().back(cabin).front(body)


def eco():
    leaf = (circle(6.8, 12, 10) & circle(17.2, 12, 10)).rotate(45).round(1.2).move(1.6, -1.4)
    stem = stroke([(4, 20), (8.2, 15.8)], 3)
    return Icon().back(leaf).front(stem)


def favorite_fill():
    lobes = circle(7.6, 9.4, 5) | circle(16.4, 9.4, 5)
    tip = poly([(3.1, 11.6), (20.9, 11.6), (12, 20.6)])
    return Icon().front((lobes | tip).round(1.4))


def fitness_center():
    bar = stroke([(8, 12), (16, 12)], 3.2)
    plates = union(
        pill(5, 5, 4.2, 14), pill(1.8, 8, 4, 8), pill(14.8, 5, 4.2, 14), pill(18.2, 8, 4, 8)
    )
    return Icon().front(bar | plates)


def flight():
    body = stroke([(12, 3), (12, 19.6)], 3.8)
    wings = poly([(12, 8.6), (21.5, 14), (21.5, 16.2), (12, 13.6), (2.5, 16.2), (2.5, 14)]).round(1)
    tail = poly([(12, 16.6), (16, 19.6), (16, 21.4), (12, 20.4), (8, 21.4), (8, 19.6)]).round(0.8)
    return Icon().front(union(body, wings, tail).round(0.8))


def tree(cx, top, bottom, half):
    crown = poly([(cx, top), (cx + half, bottom), (cx - half, bottom)]).round(2)
    trunk = pill(cx - 1.6, bottom - 2, 3.2, 5.6)
    return crown | trunk


def forest():
    back = tree(8, 2, 16.4, 6.4)
    front = tree(16.2, 6.4, 18.6, 5.6)
    return Icon().back(back).front(front)


def hiking():
    head = circle(11.2, 4, 2.4)
    torso = stroke([(10.6, 8.4), (9.8, 13.4)], 3.8)
    legs = stroke([(9.8, 13.4), (7.4, 20.6)], 3) | stroke(
        [(9.8, 13.4), (12.6, 16.4), (13, 20.8)], 3
    )
    arm = stroke([(10.8, 9.4), (13.8, 12)], 2.6)
    stick = stroke([(16, 9.6), (16, 21)], 2.6)
    pack = rbox(3, 7, 5.2, 8, 2.4)
    return Icon().back(pack).front(union(head, torso, legs, arm, stick))


def landscape():
    back = poly([(2, 20.5), (9.6, 5.6), (17.2, 20.5)]).round(1.8)
    front = poly([(9, 20.5), (15.6, 11), (22, 20.5)]).round(1.8)
    return Icon().back(back).front(front).front(circle(18.4, 5.4, 2.4))


def lightbulb():
    glass = (circle(12, 9.4, 7.4) | poly([(7.6, 13.6), (16.4, 13.6), (15, 18), (9, 18)])).round(1.5)
    base = pill(8.4, 16.4, 7.2, 2.8) | pill(9.8, 19.6, 4.4, 2.6)
    return Icon().back(glass).front(base)


def local_cafe():
    cup = rbox(3, 3.5, 13.4, 13.5, 4.6) | rbox(3, 3.5, 13.4, 6, 1.6)
    handle = ring(16.6, 8.6, 2.75, 2.1)
    saucer = pill(2.5, 18.5, 19, 3)
    return Icon().back((cup | handle).round(0.6)).front(saucer)


def local_fire_department():
    outer = (circle(12, 14, 8) | poly([(5.2, 10), (12, 1.6), (18.8, 10)])).round(2)
    inner = (circle(12, 16, 3) | poly([(9.4, 14.6), (12, 9.8), (14.6, 14.6)])).round(1)
    return Icon().back(outer).front(inner)


def local_florist():
    petals = union(*[circle(*polar(5.6, a), 4.4) for a in range(0, 360, 72)]).round(1)
    return Icon().back(petals).front(circle(12, 12, 3.2))


def local_pizza():
    crust = pill(2.5, 3, 19, 4.4)
    cheese = poly([(2.8, 4), (21.2, 4), (12, 22.4)]).round(2.2)
    pepperoni = circle(10.4, 11.4, 1.8) | circle(13.4, 14.8, 1.5)
    return Icon().back(cheese).front(crust).mark(pepperoni)


def medical_services():
    handle = rbox(8, 2.6, 8, 6.4, 2.6) - rbox(10.4, 5, 3.2, 6, 1.2)
    case = (rbox(2.5, 6.6, 19, 14.4, 4) | handle).round(0.6)
    cross = rbox(10.4, 9.4, 3.2, 9, 1.4) | rbox(7.5, 12.3, 9, 3.2, 1.4)
    return Icon().back(case).mark(cross.center_in(rbox(2.5, 6.6, 19, 14.4, 4)))


ICONS = {
    f.__name__: f
    for f in (
        ac_unit,
        account_balance,
        anchor,
        beach_access,
        bolt,
        build,
        cake,
        castle,
        cruelty_free,
        diamond,
        directions_bike,
        directions_car,
        eco,
        favorite_fill,
        fitness_center,
        flight,
        forest,
        hiking,
        landscape,
        lightbulb,
        local_cafe,
        local_fire_department,
        local_florist,
        local_pizza,
        medical_services,
    )
}
