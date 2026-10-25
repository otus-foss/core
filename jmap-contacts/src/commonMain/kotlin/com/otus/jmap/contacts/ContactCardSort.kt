package com.otus.jmap.contacts

import com.otus.jmap.JmapSort

/**
 * Comparators for `ContactCard/query` and `/queryChanges` (RFC 9610, section 3.3.2). Every server
 * must support [created] and [updated]. The name sorts are only recommended, so expect the method
 * error `unsupportedSort` and sort on the device instead. A name sort compares the first name
 * component of its kind. [JmapSort.collation] must be one of the core capability's
 * `collationAlgorithms`; null lets the server choose.
 */
public object ContactCardSort {
    /** By the card's `created` time. */
    public fun created(isAscending: Boolean = true): JmapSort = JmapSort("created", isAscending)

    /** By the card's `updated` time. */
    public fun updated(isAscending: Boolean = true): JmapSort = JmapSort("updated", isAscending)

    /** By the first `given` name component. */
    public fun nameGiven(
        isAscending: Boolean = true,
        collation: String? = null,
    ): JmapSort = JmapSort("name/given", isAscending, collation)

    /** By the first `surname` name component. */
    public fun nameSurname(
        isAscending: Boolean = true,
        collation: String? = null,
    ): JmapSort = JmapSort("name/surname", isAscending, collation)

    /** By the first `surname2` name component, a second surname. */
    public fun nameSurname2(
        isAscending: Boolean = true,
        collation: String? = null,
    ): JmapSort = JmapSort("name/surname2", isAscending, collation)
}
