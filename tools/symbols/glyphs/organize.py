"""Folders, labels, marks, filters, search and settings."""

import math

from glyph import GAP, Icon, arc, circle, poly, rbox, stroke, union


def folder_back():
    return (rbox(2.5, 3, 9.5, 9, 3) | rbox(2.5, 4.4, 19, 9, 3)).round(1.6)


def pocket():
    return rbox(2.5, 10.4, 19, 10.6, 4.5)


def folder():
    back = (rbox(2.5, 3, 9.5, 9, 3) | rbox(2.5, 4.4, 19, 9, 3)).round(1.6)
    return Icon().back(back).front(rbox(2.5, 10.4, 19, 10.6, 4.5))


def folder_open():
    front = poly([(6.2, 10.4), (22, 10.4), (18.6, 21), (2.5, 21)]).round(3.2)
    return Icon().back(folder_back()).front(front)


def folder_shared():
    head = circle(12, 14.6, 2.1)
    shoulders = rbox(8, 16.2, 8, 6, 3) & rbox(0, 0, 24, 18.5)
    person = (head | shoulders).round(0.8)
    return Icon().back(folder_back()).front((pocket() - person).round(0.6))


def folder_zip():
    slot = stroke([(12, 9), (12, 13.4)], 2.4) | rbox(9.6, 13, 4.8, 5.6, 2.2)
    back = folder_back() - pocket().grow(GAP)
    return Icon().back(back).front((pocket() - slot).round(0.6))


def label():
    tag = poly([(3, 3), (12, 3), (21.2, 12.2), (12.2, 21.2), (3, 12)]).round(2.6)
    return Icon().back(tag).mark(circle(8.2, 8.2, 2.2))


def keep():
    cap = rbox(6.5, 2.5, 11, 4, 2)
    body = rbox(7.5, 5, 9, 7, 0)
    collar = rbox(4.5, 10.6, 15, 4.2, 2.1)
    head = union(cap, body, collar).round(1.2)
    return Icon().back(head).front(stroke([(12, 14.6), (12, 21.4)], 3))


def bookmark():
    ribbon = poly([(5, 2.5), (19, 2.5), (19, 21.5), (12, 16.2), (5, 21.5)])
    return Icon().front(ribbon.round(2.6))


def star():
    points = [
        (12 + r * math.sin(math.radians(i * 36)), 12.8 - r * math.cos(math.radians(i * 36)))
        for i, r in enumerate([11.6, 5.0] * 5)
    ]
    return Icon().front(poly(points).round(1.6))


def attach_file():
    legs = (
        [(7.8, 15.5), (7.8, 6.5)]
        + arc(12, 6.5, 4.2, 180, 360)[1:]
        + [(16.2, 17.5)]
        + arc(14.1, 17.5, 2.1, 0, 180)[1:]
        + [(12, 8)]
    )
    return Icon().front(stroke(legs, 2.6).rotate(40))


def signature():
    wave = [(x / 4, 10 - 4 * math.sin((x / 4 - 4) / 15 * 2.5 * math.pi)) for x in range(16, 77)]
    return Icon().back(rbox(3, 17.6, 18, 3.4, 1.7)).front(stroke(wave, 3.2))


def funnel():
    top = poly([(2.5, 3.5), (21.5, 3.5), (14.2, 12.6), (9.8, 12.6)]).round(1.8)
    return (top | rbox(9.8, 10, 4.4, 11, 2.2)).round(1.2)


def filter_alt():
    return Icon().front(funnel())


def auto_filter():
    tips = [
        (18.2 + r * math.sin(math.radians(i * 45)), 6.2 - r * math.cos(math.radians(i * 45)))
        for i, r in enumerate([5.4, 2.4] * 4)
    ]
    return Icon().back(funnel().move(-1.6, 0.5)).front(poly(tips).round(0.9))


def bars():
    return union(*(rbox(12 - w / 2, y, w, 3.4, 1.7) for w, y in ((19, 4.6), (13, 10.3), (7, 16))))


def filter_list():
    return Icon().front(bars())


def filter_list_off():
    slash = stroke([(4.2, 3), (19.8, 21)], 3.2)
    pieces = (bars() - slash.grow(GAP)).round(1.0).without_slivers(6)
    return Icon().back(pieces).front(slash)


def tune():
    rails = union(*(rbox(2.5, y - 1.5, 19, 3, 1.5) for y in (5, 12, 19)))
    knobs = union(*(rbox(x - 2.2, y - 3.4, 4.4, 6.8, 2.2) for x, y in ((17, 5), (7, 12), (17, 19))))
    return Icon().back(rails).front(knobs)


def settings():
    teeth = [rbox(18.6, 9.2, 3.8, 5.6, 1.4).rotate(a) for a in range(0, 360, 60)]
    gear = union(circle(12, 12, 7.6), *teeth).round(1.3)
    return Icon().back(gear).mark(circle(12, 12, 3.2))


def flag():
    cloth = poly([(5.5, 3.2), (20.5, 3.2), (16.5, 8.6), (20.5, 14), (5.5, 14)]).round(2.2)
    return Icon().back(cloth).front(stroke([(5.5, 3.5), (5.5, 20.5)], 3.4))


def search():
    return Icon().back(circle(9.6, 9.6, 7.2)).front(stroke([(16.6, 16.6), (20.2, 20.2)], 4.2))


ICONS = {
    "folder": folder,
    "folder_fill": folder,
    "folder_open": folder_open,
    "folder_open_fill": folder_open,
    "folder_shared": folder_shared,
    "folder_zip": folder_zip,
    "label": label,
    "keep": keep,
    "bookmark": bookmark,
    "bookmark_fill": bookmark,
    "star": star,
    "star_fill": star,
    "attach_file": attach_file,
    "signature": signature,
    "auto_filter": auto_filter,
    "filter_alt": filter_alt,
    "filter_list": filter_list,
    "filter_list_off": filter_list_off,
    "tune": tune,
    "settings": settings,
    "flag": flag,
    "search": search,
}
