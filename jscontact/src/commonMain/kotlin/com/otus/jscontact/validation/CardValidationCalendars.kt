package com.otus.jscontact.validation

import com.otus.jscontact.Calendar
import com.otus.jscontact.SchedulingAddress

/** Validates a Calendar (RFC 9553, section 2.4.1). */
internal fun ValidationScope.checkCalendar(calendar: Calendar) {
    propertyNames(calendar.extra, Calendar.serializer())
    enumValue(calendar.kind.value, RegisteredValues.calendarKinds, "kind")
    uri(calendar.uri, "uri")
    resource(calendar)
}

/** Validates a SchedulingAddress (RFC 9553, section 2.4.2). */
internal fun ValidationScope.checkSchedulingAddress(address: SchedulingAddress) {
    propertyNames(address.extra, SchedulingAddress.serializer())
    uri(address.uri, "uri")
    contexts(address.contexts)
    pref(address.pref)
}
