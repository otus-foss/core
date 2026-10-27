package com.otus.jmap.push

import com.otus.jmap.login.InMemoryLoginStore
import com.otus.jmap.login.JmapLogin
import com.otus.jmap.login.LoginSecret
import com.otus.jmap.login.LoginStore
import com.otus.jmap.login.LoginStoreException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PushSubscriptionFailuresTest {
    @Test
    fun anEndpointWithoutKeysIsRefusedWithoutAskingTheServer() =
        runTest {
            val store = Store()
            val server = Server { created() }
            subscriptions(server, store).run {
                setEndpoint("ada", endpoint.copy(keys = null))
                sync()
                assertEquals(mapOf("ada" to PushStatus.Refused), status())
            }
            assertTrue(server.sent.isEmpty(), server.sent.toString())
        }

    @Test
    fun unreadableLoginsLeaveTheRecordsAsTheyAre() =
        runTest {
            val store = Store()
            subscribed(store)
            val before = store.records
            val server = Server { created() }
            subscriptions(server, store, logins = Broken(listsLogins = false)).sync()
            assertEquals(before, store.records)
            assertTrue(server.sent.isEmpty(), server.sent.toString())
        }

    @Test
    fun anUnreadableSecretKeepsTheRecordForTheNextSync() =
        runTest {
            val store = Store()
            subscriptions(Server { created() }, store, logins = Broken(listsLogins = true)).run {
                setEndpoint("ada", endpoint)
                sync()
                assertEquals(mapOf("ada" to PushStatus.Waiting), status())
            }
        }

    @Test
    fun forgettingWithUnreadableLoginsStillDropsTheRecord() =
        runTest {
            val store = Store()
            subscribed(store)
            subscriptions(Server { created() }, store, logins = Broken(listsLogins = false)).run {
                forget("ada")
                assertEquals(emptyMap(), status())
            }
        }

    /** Lists [login] when [listsLogins], but cannot read its secret; otherwise cannot list either. */
    private class Broken(
        private val listsLogins: Boolean,
    ) : LoginStore by InMemoryLoginStore() {
        override fun logins(): List<JmapLogin> = if (listsLogins) listOf(login) else throw LoginStoreException("locked")

        override fun secret(id: String): LoginSecret = throw LoginStoreException("locked")
    }
}
