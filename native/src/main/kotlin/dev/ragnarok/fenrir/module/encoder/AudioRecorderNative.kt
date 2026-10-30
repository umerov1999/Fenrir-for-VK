package dev.ragnarok.fenrir.module.encoder

import dev.ragnarok.fenrir.module.FenrirNative.isNativeLoaded
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class AudioRecorderNative {
    private external fun nCreate(
        outputPath: String,
        sampleRate: Int
    ): Long

    private external fun nWriteFrame(handle: Long, frame: ByteBuffer, length: Int): Boolean

    private external fun nFlush(handle: Long): Long

    private external fun nFinish(handle: Long): Boolean

    private external fun nDestroy(handle: Long)

    private external fun nGetWaveform(path: String): ByteArray?

    private external fun encodeToOpus(input: String, output: String, sampleRate: Int): Boolean

    private var nativeHandle = 0L

    fun create(
        outputPath: String,
        sampleRate: Int
    ): Boolean {
        nativeHandle = nCreate(
            outputPath,
            sampleRate
        )
        return nativeHandle != 0L
    }

    private val released = AtomicBoolean(false)

    fun writeFrame(frame: ByteBuffer, length: Int): Boolean {
        checkNotReleased()
        return nWriteFrame(nativeHandle, frame, length)
    }

    fun flush(): Long {
        checkNotReleased()
        return nFlush(nativeHandle)
    }

    fun finish(): Boolean {
        if (!released.compareAndSet(false, true)) {
            return false
        }
        val handle = nativeHandle
        nativeHandle = 0L
        return handle != 0L && nFinish(handle)
    }

    fun recycle() {
        if (!released.compareAndSet(false, true)) {
            return
        }
        val handle = nativeHandle
        nativeHandle = 0L
        if (handle != 0L) {
            nDestroy(handle)
        }
    }

    private fun checkNotReleased() {
        check(!released.get()) { "Native audio recorder is already released" }
    }

    protected fun finalize() {
        try {
            recycle()
        } catch (_: Exception) {
        }
    }

    companion object {
        fun getWaveform(path: String): ByteArray? {
            return AudioRecorderNative().nGetWaveform(path)
        }

        fun encodeToOpusAudio(input: String, output: String, sampleRate: Int): Boolean {
            return isNativeLoaded && AudioRecorderNative().encodeToOpus(input, output, sampleRate)
        }
    }
}