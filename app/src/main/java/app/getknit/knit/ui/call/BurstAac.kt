package app.getknit.knit.ui.call

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import app.getknit.knit.call.WalkiePolicy
import app.getknit.knit.data.VoiceAudio
import java.io.ByteArrayOutputStream

/**
 * A kept walkie burst as a chat voice note (ADR 2026-11.tq7q): the burst's 16 kHz mono 16-bit PCM through the
 * platform's AAC-LC encoder, each access unit behind an ADTS header ([VoiceAudio.adtsHeader]), which is the stream
 * `VoiceRecorder` writes and every build already plays, measures and transcribes. In memory only; called off the
 * capture thread, after the burst has ended.
 */
internal object BurstAac {
    /** [pcm] as an `audio/aac` voice note, or null when the encoder would not open or made nothing of it. */
    fun encode(pcm: ByteArray): ByteArray? {
        val codec = open() ?: return null
        return try {
            run(codec, pcm).takeIf { it.isNotEmpty() }
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            // The platform codec throws what it likes; the burst was already sent, only its note is lost.
            Log.w(TAG, "kept burst not encoded: ${e.javaClass.simpleName}: ${e.message}")
            null
        } finally {
            runCatching { codec.stop() }
            runCatching { codec.release() }
        }
    }

    // Feeds the whole burst, then the end mark, draining as it goes; gives up after MAX_IDLE rounds that moved nothing.
    private fun run(
        codec: MediaCodec,
        pcm: ByteArray,
    ): ByteArray {
        val out = ByteArrayOutputStream(pcm.size / EXPECTED_RATIO)
        val info = MediaCodec.BufferInfo()
        var offset = 0
        var fed = false
        var done = false
        var idle = 0
        while (!done && idle < MAX_IDLE) {
            var moved = false
            if (!fed) {
                val n = feed(codec, pcm, offset)
                if (n >= 0) {
                    offset += n
                    fed = offset >= pcm.size
                    moved = true
                }
            }
            var index = codec.dequeueOutputBuffer(info, DEQUEUE_US)
            while (index >= 0 && !done) {
                moved = true
                write(codec, index, info, out)
                codec.releaseOutputBuffer(index, false)
                done = info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                if (!done) index = codec.dequeueOutputBuffer(info, 0L)
            }
            idle = if (moved) 0 else idle + 1
        }
        return out.toByteArray()
    }

    // Queues the next piece of [pcm] from [offset], with the end mark on the last; the bytes taken, or -1 for no buffer now.
    private fun feed(
        codec: MediaCodec,
        pcm: ByteArray,
        offset: Int,
    ): Int {
        val index = codec.dequeueInputBuffer(DEQUEUE_US)
        if (index < 0) return -1
        val buffer = codec.getInputBuffer(index)
        // Whole samples only, so a piece never splits one.
        val n = if (buffer == null) 0 else minOf(buffer.capacity(), INPUT_BYTES, pcm.size - offset) / PCM_BYTES * PCM_BYTES
        buffer?.clear()
        buffer?.put(pcm, offset, n)
        val last = buffer == null || offset + n >= pcm.size || n == 0
        val ptsUs = offset.toLong() / PCM_BYTES * US_PER_S / WalkiePolicy.SAMPLE_RATE
        codec.queueInputBuffer(index, 0, n, ptsUs, if (last) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0)
        return if (last) pcm.size - offset else n
    }

    // One access unit behind its ADTS header; the codec's config buffer is what the header already says.
    private fun write(
        codec: MediaCodec,
        index: Int,
        info: MediaCodec.BufferInfo,
        out: ByteArrayOutputStream,
    ) {
        if (info.size <= 0 || info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) return
        val buffer = codec.getOutputBuffer(index) ?: return
        val header = VoiceAudio.adtsHeader(info.size, WalkiePolicy.SAMPLE_RATE) ?: return
        val frame = ByteArray(info.size)
        buffer.position(info.offset)
        buffer.get(frame)
        out.write(header)
        out.write(frame)
    }

    private fun open(): MediaCodec? =
        runCatching {
            val format =
                MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, WalkiePolicy.SAMPLE_RATE, 1).apply {
                    setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                    setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
                    setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, INPUT_BYTES)
                }
            MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC).apply {
                configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                start()
            }
        }.onFailure { Log.w(TAG, "kept burst AAC encoder failed: ${it.javaClass.simpleName}: ${it.message}") }.getOrNull()

    private const val TAG = "KnitCall"

    // Three quarters of a chat voice note's rate: a 30 s burst is about 93 KB, under the 128 KiB bulk-link threshold.
    private const val BIT_RATE = 24_000
    private const val PCM_BYTES = 2
    private const val INPUT_BYTES = 4096
    private const val US_PER_S = 1_000_000L
    private const val DEQUEUE_US = 10_000L

    // Rounds in a row with no buffer either way before the encoder is given up on: about four seconds.
    private const val MAX_IDLE = 200

    // PCM at 256 kbit/s against AAC at 24: the output buffer starts near its final size.
    private const val EXPECTED_RATIO = 8
}
