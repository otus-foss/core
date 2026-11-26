"""Mail actions and mail folders."""

from glyph import Icon, Shape, arc, circle, pill, poly, rbox, stroke, union


def below(y0):
    return Shape(lambda x, y: y0 - y)


def envelope():
    body = rbox(2.5, 4.5, 19, 15, 4.5)
    flap = (body & poly([(0, 4.5), (24, 4.5), (12, 13.6)])).round(1.6)
    return body, flap


def mail():
    body, flap = envelope()
    return Icon().back(body).front(flap)


def mark_email_read():
    return mail().front(stroke([(15.6, 19), (17.5, 20.9), (21.2, 17.2)], 3))


def mark_email_unread():
    return mail().front(circle(20, 5.2, 3.2))


def unsubscribe():
    return mail().front(pill(14.6, 18.2, 8, 3.4))


def drive_file_move():
    folder = (rbox(2.5, 3, 9.5, 9, 3) | rbox(2.5, 5.6, 19, 15.4, 4.5)).round(1.6)
    arrow = stroke([(7.5, 13.6), (15, 13.6)], 3) | poly(
        [(12.4, 9.4), (17.4, 13.6), (12.4, 17.8)]
    ).round(1.2)
    return Icon().back(folder).mark(arrow)


def inbox():
    square = rbox(3, 3, 18, 18, 5.5)
    tray = (square & below(13.5) - circle(12, 13.5, 3.6)).round(1)
    return Icon().back(square).front(tray)


def all_inbox():
    tray = (rbox(3, 11.5, 18, 9.5, 4.5) - circle(12, 11.5, 3.4)).round(1)
    return Icon().back(pill(6.5, 2.5, 11, 3)).back(pill(4.5, 7, 15, 3)).front(tray)


def pen():
    """A one-tone pen pointing down-left."""
    body = union(
        rbox(8.4, 1, 7.2, 10, 3.6),
        rbox(8.4, 7, 7.2, 7.6),
        poly([(8.4, 14.5), (15.6, 14.5), (12, 22.8)]),
    ).round(1.3)
    return body.rotate(45)


def edit():
    return Icon().front(pen())


def draft():
    page = (rbox(4, 2.5, 16, 19, 4.5) - poly([(10.5, 0), (24, 0), (24, 13.5)])).round(1)
    fold = poly([(13, 2.5), (20, 9.5), (13, 9.5)]).round(1.2)
    return Icon().front(page).back(fold)


def plane():
    return poly([(19.4, 4.6), (4.6, 10.8), (10.8, 13.2), (13.2, 19.4)]).grow(1.4)


def send():
    return Icon().front(plane())


def schedule_send():
    face = circle(16.8, 16.8, 5.8)
    hands = stroke([(16.8, 14), (16.8, 17), (18.8, 18.2)], 2.6)
    return Icon().back(face).front(plane().scale(0.8, origin=(3, 3))).mark(hands)


def archive():
    box = rbox(4, 7, 16, 14, 4.5)
    slot = pill(9, 13.6, 6, 2.8).center_in(box, axis="x")
    return Icon().back(box).front(rbox(2.5, 3, 19, 6, 3)).mark(slot)


def error():
    disc = circle(12, 12, 10)
    mark = pill(10.4, 6, 3.2, 7.6) | circle(12, 17.2, 1.8)
    return Icon().back(disc).mark(mark.center_in(disc))


def report():
    return error()


def block():
    disc = circle(12, 12, 10)
    return Icon().back(disc).mark(stroke([(8.6, 8.6), (15.4, 15.4)], 3.4).center_in(disc))


def delete():
    body = poly([(4.4, 6), (19.6, 6), (18.2, 21), (5.8, 21)]).round(3)
    return Icon().back(body).front(pill(3.5, 3, 17, 4.6))


def delete_sweep():
    handle = stroke([(12, 2), (12, 10.6)], 3.4)
    head = poly([(7.5, 11.5), (16.5, 11.5), (20.5, 21.5), (3.5, 21.5)]).round(2.6)
    return Icon().back(head.rotate(30)).front(handle.rotate(30))


def reply_head():
    return poly([(2.6, 10.5), (10.2, 3.6), (10.2, 17.4)]).round(1.6)


def reply_arrow():
    tail = stroke([(9, 10.5)] + arc(14.5, 16.5, 6, -90, 0)[1:] + [(20.5, 19.5)], 4.2)
    return (reply_head() | tail).round(0.8)


def reply():
    return Icon().front(reply_arrow())


def forward():
    return Icon().front(reply_arrow().mirror())


def reply_all():
    arrow = reply_arrow().scale(0.74, origin=(22.5, 12))
    return Icon().back(reply_head().scale(0.74, origin=(1.4, 12))).front(arrow)


ICONS = {
    "inbox": inbox,
    "all_inbox": all_inbox,
    "edit": edit,
    "draft": draft,
    "send": send,
    "schedule_send": schedule_send,
    "archive": archive,
    "error": error,
    "report": report,
    "block": block,
    "delete": delete,
    "delete_sweep": delete_sweep,
    "reply": reply,
    "reply_all": reply_all,
    "forward": forward,
    "mail": mail,
    "mark_email_read": mark_email_read,
    "mark_email_unread": mark_email_unread,
    "unsubscribe": unsubscribe,
    "drive_file_move": drive_file_move,
}
