package dev.ragnarok.fenrir.module.hls

import dev.ragnarok.fenrir.module.FenrirNative.isNativeLoaded

object TSDemuxer {
    private external fun unpack(
        input: String,
        output: String,
        info: Boolean,
        printDebug: Boolean
    ): Boolean

    fun unpackTS(input: String, output: String, info: Boolean, printDebug: Boolean): Boolean {
        return isNativeLoaded && unpack(input, output, info, printDebug)
    }
}