package dev.ragnarok.fenrir.module.encoder

import dev.ragnarok.fenrir.module.FenrirNative.isNativeLoaded
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class AudioNoiseSuppressor {
    private external fun nCreate(): Long

    private external fun nProcess(handle: Long, pcm: ByteBuffer, sampleCount: Int): Boolean

    private external fun nDestroy(handle: Long)
    private var nativeHandle = 0L

    fun create(): Boolean {
        if (!isNativeLoaded) {
            return false
        }
        nativeHandle = nCreate()
        return nativeHandle != 0L
    }

    private val released = AtomicBoolean(false)

    fun process(pcm: ByteBuffer, sampleCount: Int): Boolean {
        check(!released.get()) { "Native noise suppressor is already released" }
        return nProcess(nativeHandle, pcm, sampleCount)
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

    protected fun finalize() {
        try {
            recycle()
        } catch (_: Exception) {
        }
    }
}