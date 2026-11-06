package com.otus.avatar.ui.presentation

import com.otus.avatar.AvatarMark
import com.otus.avatar.AvatarStyle
import com.otus.avatar.ContactAvatar
import com.otus.avatar.OwnAvatarException
import com.otus.avatar.OwnAvatarFailure
import com.otus.avatar.OwnAvatars
import com.otus.avatar.ResolvedAvatar
import com.otus.avatar.generatedAvatar
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class OwnAvatarsStateTest {
    private val account = OwnAccount("ada", "Ada", listOf("ada@example.org"))
    private val first = styled("AL")
    private val second = styled("AD")

    @Test
    fun quickSavesRunOneAtATimeAndKeepTheNewest() =
        runTest {
            val server = GatedAvatars()
            val state = OwnAvatarsState(server, photos = null, this)
            state.save(account, "ada@example.org", first)
            state.save(account, "ada@example.org", second)
            advanceUntilIdle()
            assertEquals(1, server.waiting.size)
            server.waiting.removeFirst().complete(Unit)
            advanceUntilIdle()
            assertEquals(second.style, state.avatar(account, "ada@example.org")?.style)
            server.waiting.removeFirst().complete(Unit)
            advanceUntilIdle()
            assertEquals(1, server.mostAtOnce)
            assertEquals(second.style, state.avatar(account, "ada@example.org")?.style)
        }

    @Test
    fun aFailedSaveGoesBackToTheLastSavedAvatar() =
        runTest {
            val server = GatedAvatars()
            val state = OwnAvatarsState(server, photos = null, this)
            state.save(account, "ada@example.org", first)
            state.save(account, "ada@example.org", second)
            advanceUntilIdle()
            server.waiting.removeFirst().complete(Unit)
            advanceUntilIdle()
            server.failing = true
            server.waiting.removeFirst().complete(Unit)
            advanceUntilIdle()
            assertEquals(first.style, state.avatar(account, "ada@example.org")?.style)
            assertNotNull(state.error)
        }

    private fun styled(letters: String) = ContactAvatar.Styled(AvatarStyle("blue", AvatarMark.Letters(letters)))
}

/** Holds each save until the test completes its gate, and keeps the last saved avatar. */
private class GatedAvatars : OwnAvatars {
    val waiting = ArrayDeque<CompletableDeferred<Unit>>()
    var failing = false
    var mostAtOnce = 0
    private var running = 0
    private var stored: ContactAvatar? = null

    override suspend fun avatar(
        accountKey: String,
        email: String,
        name: String,
    ) = ResolvedAvatar(generatedAvatar(name, email), style = (stored as? ContactAvatar.Styled)?.style)

    override suspend fun save(
        accountKey: String,
        email: String,
        name: String,
        avatar: ContactAvatar?,
    ) {
        running++
        mostAtOnce = maxOf(mostAtOnce, running)
        try {
            val gate = CompletableDeferred<Unit>()
            waiting += gate
            gate.await()
            if (failing) throw OwnAvatarException(OwnAvatarFailure.Failed)
            stored = avatar
        } finally {
            running--
        }
    }
}
