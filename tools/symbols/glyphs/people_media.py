"""People, communication, devices, media, calendar and time."""

from glyph import Icon, arc, circle, poly, rbox, stroke, union

# Helpers ---------------------------------------------------------------------------------------


def closing(shape, r):
    """Fills concave corners only, so small parts such as the bell's knob survive."""
    return shape.grow(r).grow(-r)


def person_parts(dx=0.0, dy=0.0, s=1.0):
    """Head and shoulders of one person, 1.5 apart; scale s shrinks it toward its feet."""
    head = circle(12, 7.6, 4.3)
    shoulders = (circle(12, 22.0, 8.6) & rbox(0, 0, 24, 21)).round(1.8)
    return tuple(g.scale(s, origin=(12, 21)).move(dx, dy) for g in (head, shoulders))


def person_with(badge):
    """A person shifted left with a front badge that knocks both parts out."""
    head, shoulders = person_parts(dx=-2.2)
    return Icon().back(shoulders).front(head).front(badge)


def plus(cx, cy, arm=3.6, width=3.2):
    return stroke([(cx - arm, cy), (cx + arm, cy)], width) | stroke(
        [(cx, cy - arm), (cx, cy + arm)], width
    )


def minus(cx, cy, arm=3.6, width=3.2):
    return stroke([(cx - arm, cy), (cx + arm, cy)], width)


def small_pen(cx, cy, length=8.6, width=4.4):
    half = width / 2
    body = rbox(-half, -length / 2 - 1, width, length, half) | poly(
        [(-half, length / 2 - 2), (half, length / 2 - 2), (0, length / 2 + 2.8)]
    )
    # Round after moving: round() samples the grid, so the shape must already sit on it.
    return body.rotate(45, origin=(0, 0)).move(cx, cy).round(0.9)


def small_lens(cx, cy):
    ring = circle(cx, cy, 3.6) - circle(cx, cy, 1.1)
    return ring | stroke([(cx + 2.7, cy + 2.7), (cx + 4.6, cy + 4.6)], 2.8)


def slash(end=15.6, width=3.0):
    """A slash from the top-left corner that stops inside the shape, so it notches the shape
    instead of cutting it in two."""
    return stroke([(3.2, 3.2), (end, end)], width)


def calendar(marks, badge=None, size=(2.5, 4.5, 19, 17)):
    """One calendar body with two ring tabs; marks sit on it, a badge knocks everything out."""
    x, y, w, h = size
    rings = union(*(stroke([(x + w * f, y - 2), (x + w * f, y + 1.2)], 3.2) for f in (0.3, 0.7)))
    icon = Icon().back(rbox(x, y, w, h, 4.5)).front(rings).mark(union(*marks))
    return icon.front(badge) if badge is not None else icon


def photo_frame(x=2.5, y=3, w=19, h=18):
    return rbox(x, y, w, h, 4.5)


def scenery(dx=0.0, dy=0.0, s=1.0, sun=True):
    hills = poly([(5.6, 17.6), (9.6, 11.6), (12.8, 15.4), (15.2, 12.6), (18.4, 17.6)]).round(1.2)
    parts = hills | circle(16, 7.8, 2) if sun else hills
    return parts.scale(s).move(dx, dy)


# People ----------------------------------------------------------------------------------------


def person():
    head, shoulders = person_parts()
    return Icon().back(shoulders).front(head)


def account_circle():
    disc = circle(12, 12, 10)
    head = circle(12, 9.8, 3.3)
    shoulders = circle(12, 22.6, 7.6) & disc
    return Icon().back(disc).front(head | shoulders)


def person_add():
    return person_with(plus(18.6, 17.8, arm=3.2))


def person_edit():
    return person_with(small_pen(18.6, 17.4))


def person_search():
    return person_with(small_lens(17.4, 16.2))


def group_icon(dx=0.0, s=0.9):
    head, shoulders = person_parts(dx=-3 + dx, s=s)
    back_head, back_shoulders = person_parts(dx=3.6 + dx, dy=-1.6, s=s - 0.08)
    return Icon().back(back_head | back_shoulders).back(shoulders).front(head)


def group():
    return group_icon()


def group_add():
    return group_icon(dx=-0.8, s=0.82).front(plus(18.6, 18, arm=3))


def group_remove():
    return group_icon(dx=-0.8, s=0.82).front(minus(18.6, 18, arm=3))


def contacts():
    card = rbox(2.5, 3, 19, 18, 4.5)
    person_mark = circle(12, 9.6, 3.2) | rbox(6.4, 14.2, 11.2, 4.6, 2.3)
    return Icon().back(card).mark(person_mark)


def badge():
    card = rbox(2.5, 6, 19, 15, 4.5)
    clip = rbox(8.6, 2.4, 6.8, 6.4, 2.6)
    details = union(
        circle(8.4, 13.4, 2.4),
        rbox(5, 16.4, 6.8, 2.6, 1.3),
        rbox(13.4, 11.6, 5.4, 2.6, 1.3),
        rbox(13.4, 15.4, 3.8, 2.6, 1.3),
    )
    return Icon().back(card).front(clip).mark(details)


# Communication ---------------------------------------------------------------------------------


def alternate_email():
    inner = circle(12, 12, 4.6) - circle(12, 12, 1.8)
    tail = (
        [(15.2, 8.2), (15.2, 13.4)]
        + arc(17.9, 13.4, 2.7, 180, 0)[1:]
        + arc(12, 12, 8.6, 9, -290)[1:]
    )
    return Icon().front(inner | stroke(tail, 3))


def call():
    cx, cy, r = 17, 7, 10.4
    body = stroke(arc(cx, cy, r, 98, 172), 4.2)
    pads = [stroke(arc(cx, cy, r + 0.6, a, b), 6.2) for a, b in ((96, 108), (162, 174))]
    return Icon().front(union(body, *pads).round(1.2))


def bubble(x, y, w, h, tail):
    return (rbox(x, y, w, h, 4.2) | poly(tail)).round(1.2)


def forum():
    back = bubble(2.5, 3, 12.5, 9.2, [(3.2, 10), (7.8, 10), (3.2, 14.6)])
    front = bubble(9, 9.4, 12.5, 9.2, [(16, 16.6), (20.8, 16.6), (20.8, 21.2)])
    return Icon().back(back).front(front)


def handshake():
    back = rbox(1.6, 5.6, 4.6, 10, 2.3) | stroke([(6.6, 9.2), (10.6, 7.4), (16.6, 13.2)], 4.2)
    front = union(
        rbox(17.8, 5.6, 4.6, 10, 2.3),
        stroke([(17.4, 9.2), (13.8, 9.2), (8.6, 14.4)], 4.2),
        stroke([(11.4, 16.6), (13, 18.2)], 3.4),
    )
    return Icon().back(back).front(front)


def bell():
    dome = rbox(5.4, 3.2, 13.2, 14, 6.6)
    flare = rbox(3, 14.2, 18, 3.8, 1.9)
    knob = circle(12, 3.4, 1.8)
    return closing(union(dome, flare, knob), 2.2), rbox(9.2, 19.4, 5.6, 2.8, 1.4)


def notifications():
    body, clapper = bell()
    return Icon().back(body).front(clapper)


def notifications_off():
    body, clapper = bell()
    return Icon().back(body).front(clapper).front(slash(end=11.6))


# Devices ---------------------------------------------------------------------------------------


def smartphone():
    return Icon().back(rbox(5.5, 2, 13, 20, 4.2)).mark(rbox(9.4, 17, 5.2, 2.8, 1.4))


def computer():
    return Icon().back(rbox(6.6, 18.2, 10.8, 3, 1.5)).front(rbox(2, 3.5, 20, 13.4, 3.8))


def mic():
    holder = stroke(arc(12, 10.6, 7.6, 0, 180), 3) | stroke([(12, 18.2), (12, 21.4)], 3)
    return Icon().back(holder).front(rbox(8, 2, 8, 13.4, 4))


def headphones():
    band = stroke(arc(12, 13, 8.4, 180, 360), 3.2)
    cups = rbox(2.4, 12.6, 6, 9, 3) | rbox(15.6, 12.6, 6, 9, 3)
    return Icon().back(band).front(cups)


# Media -----------------------------------------------------------------------------------------


def photo_camera():
    body = (rbox(2, 6, 20, 15, 4.5) | rbox(7.6, 3, 8.8, 5.4, 2.2)).round(1.2)
    return Icon().back(body).front(circle(12, 13.6, 3.8))


def image():
    return Icon().back(photo_frame()).mark(scenery())


def add_photo_alternate():
    frame = photo_frame(2.5, 4.5, 17, 17)
    return Icon().back(frame).mark(scenery(-1, 1.6, 0.9, sun=False)).front(plus(18.4, 5.6, arm=3.2))


def hide_image():
    return Icon().back(photo_frame()).mark(scenery()).front(slash())


def pdf():
    return Icon().back(rbox(6, 2, 14, 20, 4.5)).front(rbox(3, 12.4, 12.4, 6.4, 3.2))


def slideshow():
    screen = rbox(2.5, 3.5, 19, 17, 4.5)
    play = poly([(9.4, 7.6), (16.6, 12), (9.4, 16.4)]).round(1.2)
    return Icon().back(screen).mark(play.center_in(screen))


# Calendar and time -----------------------------------------------------------------------------


def calendar_today():
    return calendar([rbox(6.6, 12.4, 5.2, 5.2, 1.8)])


def calendar_month():
    return calendar([circle(x, y, 1.5) for y in (12.8, 17) for x in (7.4, 12, 16.6)])


def event():
    return calendar([rbox(12.2, 12.6, 5.6, 5.2, 1.8)])


def calendar_add_on():
    return calendar(
        [rbox(6.4, 11.8, 4.8, 4.6, 1.6)], badge=plus(18.2, 18, arm=3), size=(2.5, 5, 16.5, 15)
    )


def edit_calendar():
    return calendar(
        [rbox(6.4, 11.8, 4.8, 4.6, 1.6)], badge=small_pen(18.6, 18), size=(2.5, 4.5, 17, 15.5)
    )


def schedule():
    # The hands pivot on the face's center, which is how a clock centers them.
    return Icon().back(circle(12, 12, 10)).mark(stroke([(12, 7), (12, 12), (15.6, 14.2)], 3))


def hourglass_empty():
    caps = rbox(4.6, 2, 14.8, 3.4, 1.7) | rbox(4.6, 18.6, 14.8, 3.4, 1.7)
    glass = poly(
        [
            (6, 4),
            (18, 4),
            (18, 7.4),
            (13.6, 12),
            (18, 16.6),
            (18, 20),
            (6, 20),
            (6, 16.6),
            (10.4, 12),
            (6, 7.4),
        ]
    ).round(1.6)
    return Icon().back(glass).front(caps)


ICONS = {
    "account_circle": account_circle,
    "person": person,
    "person_add": person_add,
    "person_edit": person_edit,
    "person_search": person_search,
    "group": group,
    "group_add": group_add,
    "group_remove": group_remove,
    "contacts": contacts,
    "badge": badge,
    "alternate_email": alternate_email,
    "call": call,
    "forum": forum,
    "handshake": handshake,
    "notifications": notifications,
    "notifications_off": notifications_off,
    "smartphone": smartphone,
    "computer": computer,
    "mic": mic,
    "headphones": headphones,
    "photo_camera": photo_camera,
    "image": image,
    "add_photo_alternate": add_photo_alternate,
    "hide_image": hide_image,
    "pdf": pdf,
    "slideshow": slideshow,
    "calendar_add_on": calendar_add_on,
    "calendar_month": calendar_month,
    "calendar_today": calendar_today,
    "edit_calendar": edit_calendar,
    "event": event,
    "schedule": schedule,
    "hourglass_empty": hourglass_empty,
}
