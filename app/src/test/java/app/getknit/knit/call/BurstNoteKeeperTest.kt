package app.getknit.knit.call

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * A mesh call's kept bursts (ADR 2026-11.tq7q): only bursts spoken with the switch on and kept by the session become
 * voice messages, each to its own peer, in the order spoken, and only once the session ends.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BurstNoteKeeperTest {
    private val sent = mutableListOf<Pair<String, Int>>()

    // The "encoder" answers the PCM's length, so a note names the burst it came from.
    private fun TestScope.keeper(
        on: () -> Boolean = { true },
        maxHeldBytes: Int = BurstNoteKeeper.MAX_HELD_BYTES,
    ) = BurstNoteKeeper(
        on = on,
        encode = { pcm -> ByteArray(4) { i -> (pcm.size shr (8 * i)).toByte() } },
        send = { peer, note ->
            sent += peer to ByteBuffer.wrap(note).order(ByteOrder.LITTLE_ENDIAN).int
            true
        },
        scope = backgroundScope,
        dispatcher = StandardTestDispatcher(testScheduler),
        maxHeldBytes = maxHeldBytes,
    )

    private fun BurstNoteKeeper.speak(
        ms: Int,
        peer: String = "bob",
        keep: Boolean = true,
    ) {
        begin()
        pcm(ByteBuffer.wrap(ByteArray(ms * BYTES_PER_MS)), WalkiePolicy.SAMPLE_RATE, 1)
        end(peer, keep)
    }

    @Test
    fun `kept bursts go out in order only when the session ends`() =
        runTest {
            val k = keeper()
            k.speak(1_000)
            k.speak(2_000, peer = "ada")
            advanceUntilIdle()
            assertTrue("nothing goes during the call", sent.isEmpty())
            k.flush()
            advanceUntilIdle()
            assertEquals(listOf("bob" to 1_000 * BYTES_PER_MS, "ada" to 2_000 * BYTES_PER_MS), sent)
        }

    @Test
    fun `a dropped burst, a short one and one spoken with the switch off keep nothing`() =
        runTest {
            var on = true
            val k = keeper(on = { on })
            k.speak(1_000, keep = false)
            k.speak(300)
            on = false
            k.speak(1_000)
            k.flush()
            advanceUntilIdle()
            assertTrue(sent.isEmpty())
        }

    @Test
    fun `audio at another rate is not kept at the wrong speed`() =
        runTest {
            val k = keeper()
            k.begin()
            k.pcm(ByteBuffer.wrap(ByteArray(32_000)), 48_000, 1)
            k.end("bob", keep = true)
            k.flush()
            advanceUntilIdle()
            assertTrue(sent.isEmpty())
        }

    @Test
    fun `past the held cap the oldest goes out early`() =
        runTest {
            val k = keeper(maxHeldBytes = 6)
            k.speak(1_000)
            k.speak(2_000)
            advanceUntilIdle()
            assertEquals(listOf("bob" to 1_000 * BYTES_PER_MS), sent)
        }

    private companion object {
        const val BYTES_PER_MS = 32
    }
}
