package app.getknit.knit.call

import app.getknit.knit.mesh.runRound
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/**
 * What a walkie session does with the bursts this phone speaks when the person keeps them (ADR 2026-11.tq7q, the Calls
 * screen's "Save what I say in mesh calls"): each one becomes a voice message in the peer's thread once the call ends.
 * The session calls these in order, one burst at a time; none of them waits.
 */
interface BurstNotes {
    /** A burst begins: its audio is taken from here on, where the switch is on. Called before the microphone opens. */
    fun begin()

    /** The burst to [peerId] ended. [keep] false drops it (a slipped thumb, a cancel); true holds it for [flush]. */
    fun end(
        peerId: String,
        keep: Boolean,
    )

    /** The session ended: every burst held goes out, oldest first. */
    fun flush()
}

/**
 * [BurstNotes] over the recorder's tap (ADR 2026-11.tq7q). While a burst is spoken its PCM, as the Opus encoder gets
 * it, is copied here; at its end [encode] makes a voice note of it (AAC in ADTS, what `VoiceRecorder` writes), and
 * when the session ends [send] posts each note to its peer as an ordinary sealed voice message, which the mesh
 * custodies like any other. The switch ([on]) is read once per burst, as it begins.
 *
 * Only this phone's own voice is ever kept, and only by its own switch: nothing a peer sends starts, names or fills a
 * note, and a burst heard is never copied. Notes wait in memory until the call ends, so a note's blob never competes
 * with the call's own chunks for a slow link; a process that dies mid-call loses the ones it held. Past [maxHeldBytes]
 * the oldest goes out early.
 *
 * One coroutine on [dispatcher] encodes and sends, in the order the bursts were spoken; the tap only copies.
 */
class BurstNoteKeeper(
    private val on: () -> Boolean,
    private val encode: (ByteArray) -> ByteArray?,
    private val send: suspend (peerId: String, note: ByteArray) -> Boolean,
    scope: CoroutineScope,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val maxHeldBytes: Int = MAX_HELD_BYTES,
    private val log: (String) -> Unit = {},
) : BurstNotes,
    TalkPcmTap {
    private sealed interface Work {
        class Encode(
            val peerId: String,
            val pcm: ByteArray,
        ) : Work

        data object Flush : Work
    }

    private class Held(
        val peerId: String,
        val note: ByteArray,
    )

    private val inbox = Channel<Work>(Channel.UNLIMITED)

    // The burst being spoken; null between bursts and while the switch is off. The tap writes on the capture thread.
    private val lock = Any()
    private var tape: ByteArrayOutputStream? = null

    // The worker's alone.
    private val held = ArrayDeque<Held>()
    private var heldBytes = 0

    init {
        scope.launch(dispatcher) {
            for (work in inbox) {
                runRound(onFailure = { e -> log("walkie: a kept burst was lost: ${e.javaClass.simpleName}") }) { handle(work) }
            }
        }
    }

    override fun begin() {
        val keeping = on()
        synchronized(lock) { tape = if (keeping) ByteArrayOutputStream() else null }
    }

    override fun pcm(
        pcm: ByteBuffer,
        rate: Int,
        channels: Int,
    ) {
        synchronized(lock) {
            val t = tape ?: return
            // Anything but what the note's encoder is set for is not kept at all, rather than kept at the wrong speed.
            if (rate != WalkiePolicy.SAMPLE_RATE || channels != 1) {
                tape = null
                return
            }
            val n = minOf(pcm.remaining(), MAX_PCM_BYTES - t.size())
            if (n <= 0) return
            val bytes = ByteArray(n)
            pcm.duplicate().get(bytes)
            t.write(bytes, 0, n)
        }
    }

    override fun end(
        peerId: String,
        keep: Boolean,
    ) {
        val pcm =
            synchronized(lock) {
                val t = tape
                tape = null
                t
            }?.toByteArray() ?: return
        if (!keep || pcm.size < MIN_PCM_BYTES) return
        inbox.trySend(Work.Encode(peerId, pcm))
    }

    override fun flush() {
        inbox.trySend(Work.Flush)
    }

    private suspend fun handle(work: Work) {
        when (work) {
            is Work.Encode -> {
                val note = encode(work.pcm)
                if (note == null || note.isEmpty()) {
                    log("walkie: a burst of ${work.pcm.size / PCM_BYTES_PER_MS} ms was not kept: no encoder took it")
                    return
                }
                held.addLast(Held(work.peerId, note))
                heldBytes += note.size
                while (heldBytes > maxHeldBytes) sendOldest()
            }

            Work.Flush -> {
                while (held.isNotEmpty()) sendOldest()
            }
        }
    }

    private suspend fun sendOldest() {
        val next = held.removeFirstOrNull() ?: return
        heldBytes -= next.note.size
        val sent = send(next.peerId, next.note)
        log("walkie: a kept burst of ${next.note.size} B ${if (sent) "went to" else "was refused for"} ${next.peerId}'s thread")
    }

    companion object {
        /** Notes held for a call's end before the oldest goes out early: about twenty minutes of talk. */
        const val MAX_HELD_BYTES = 4 * 1024 * 1024

        // 16 kHz mono 16-bit: 32 bytes a millisecond.
        private const val PCM_BYTES_PER_MS = WalkiePolicy.SAMPLE_RATE / 1000 * 2

        // Under half a second nothing was said: the session's own slipped-thumb rule, on the audio itself.
        private const val MIN_PCM_BYTES = 500 * PCM_BYTES_PER_MS

        // A burst stops at MAX_BURST_MS; the slack is the encoder's tail. Past it the tape takes no more.
        private const val MAX_PCM_BYTES = (WalkiePolicy.MAX_BURST_MS.toInt() + 2_000) * PCM_BYTES_PER_MS
    }
}
