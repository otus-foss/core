package com.otus.jmap

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * The `filter` of a `/query` or `/queryChanges` call (RFC 8620, section 5.5): a type-specific
 * [Condition], or an [Operator] that combines other filters. Combine them with [allOf], [anyOf],
 * [noneOf], [and], and [or], and send [toJson].
 */
public sealed interface JmapFilter {
    /** The filter as the `filter` argument carries it. */
    public fun toJson(): JsonObject

    /**
     * A FilterCondition: the properties the type's `/query` defines, such as `{"hasKeyword": "music"}`.
     * It cannot have an `operator` property, which marks a FilterOperator instead.
     */
    public data class Condition(
        val properties: JsonObject,
    ) : JmapFilter {
        init {
            require("operator" !in properties) { "a filter condition cannot have an operator" }
        }

        override fun toJson(): JsonObject = properties
    }

    /** A FilterOperator: [conditions] combined by [operator]. A condition can be another operator. */
    public data class Operator(
        val operator: FilterOperator,
        val conditions: List<JmapFilter>,
    ) : JmapFilter {
        override fun toJson(): JsonObject =
            buildJsonObject {
                put("operator", JsonPrimitive(operator.name))
                put("conditions", JsonArray(conditions.map { it.toJson() }))
            }
    }
}

/** How a FilterOperator combines its conditions (RFC 8620, section 5.5). Each is sent by its name. */
public enum class FilterOperator {
    /** Every condition matches. */
    AND,

    /** At least one condition matches. */
    OR,

    /** No condition matches, so several conditions act as NOR rather than a negated AND. */
    NOT,
}

/** The FilterCondition `{name: value}`, such as `{"inMailbox": "m1"}`. */
public fun filterCondition(
    name: String,
    value: JsonElement,
): JmapFilter = JmapFilter.Condition(buildJsonObject { put(name, value) })

/** Matches when every one of [filters] matches. One filter stays itself, which means the same. */
public fun allOf(filters: List<JmapFilter>): JmapFilter =
    filters.singleOrNull() ?: JmapFilter.Operator(FilterOperator.AND, filters)

public fun allOf(vararg filters: JmapFilter): JmapFilter = allOf(filters.toList())

/** Matches when at least one of [filters] matches. One filter stays itself, which means the same. */
public fun anyOf(filters: List<JmapFilter>): JmapFilter =
    filters.singleOrNull() ?: JmapFilter.Operator(FilterOperator.OR, filters)

public fun anyOf(vararg filters: JmapFilter): JmapFilter = anyOf(filters.toList())

/** Matches when none of [filters] matches. */
public fun noneOf(filters: List<JmapFilter>): JmapFilter = JmapFilter.Operator(FilterOperator.NOT, filters)

public fun noneOf(vararg filters: JmapFilter): JmapFilter = noneOf(filters.toList())

/** Matches when both filters match. A chain such as `a and b and c` stays one flat `AND`. */
public infix fun JmapFilter.and(other: JmapFilter): JmapFilter = joined(FilterOperator.AND, other)

/** Matches when either filter matches. A chain such as `a or b or c` stays one flat `OR`. */
public infix fun JmapFilter.or(other: JmapFilter): JmapFilter = joined(FilterOperator.OR, other)

private fun JmapFilter.joined(
    operator: FilterOperator,
    other: JmapFilter,
): JmapFilter = JmapFilter.Operator(operator, operands(operator) + other.operands(operator))

// AND and OR are associative, so merging nested operators of the same kind keeps the meaning.
private fun JmapFilter.operands(operator: FilterOperator): List<JmapFilter> =
    if (this is JmapFilter.Operator && this.operator == operator) conditions else listOf(this)
