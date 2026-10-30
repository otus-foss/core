package com.otus.jmap

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class JmapDecodingTest {
    @Test
    fun customDecoderShapeErrorsBecomeNotJmap() {
        val method =
            object : JmapMethod<String>("Core/echo", JmapCapability.CORE, accountId = null) {
                override fun arguments() = JsonObject(emptyMap())

                override fun decode(arguments: JsonObject) = (arguments["hello"] as JsonPrimitive).content
            }
        val call = JmapRequestBuilder().call(method)
        val response =
            parseResponse(parseObject("""{"methodResponses":[["Core/echo",{"hello":{}},"c0"]],"sessionState":"s1"}"""))
        assertFailsWith<JmapException.NotJmap> { response[call] }
        assertFailsWith<JmapException.NotJmap> { jmapDecoding { emptyMap<String, String>().getValue("missing") } }
    }

    @Test
    fun cancellationAndExistingJmapErrorsKeepTheirIdentity() {
        val cancelled = CancellationException("cancelled")
        assertSame(cancelled, assertFailsWith<CancellationException> { jmapDecoding { throw cancelled } })
        val rejected = JmapException.MethodError("forbidden", description = null)
        assertSame(rejected, assertFailsWith<JmapException.MethodError> { jmapDecoding { throw rejected } })
    }

    @Test
    fun queryCountersAcceptTheFullUnsignedRange() {
        for (value in listOf(0L, 2_147_483_648L, 9_007_199_254_740_991L)) {
            val result = query(value, value)
            assertEquals(value, result.position)
            assertEquals(value, result.total)
        }
        assertEquals(null, query(position = 0, total = null).total)
    }

    @Test
    fun queryCountersRejectNegativeAndUnsafeIntegers() {
        for (value in listOf(-1L, 9_007_199_254_740_992L, Long.MAX_VALUE)) {
            assertFailsWith<JmapException.NotJmap> { query(value, total = null) }
            assertFailsWith<JmapException.NotJmap> { query(position = 0, value) }
        }
    }

    private fun query(
        position: Long,
        total: Long?,
    ): QueryResult =
        jmapDecoding {
            QueryMethod(MAILBOX, "d1").decode(
                parseObject(
                    """{"accountId":"d1","queryState":"q1","canCalculateChanges":false,
                    "position":$position,"ids":[],"total":$total}""",
                ),
            )
        }
}
