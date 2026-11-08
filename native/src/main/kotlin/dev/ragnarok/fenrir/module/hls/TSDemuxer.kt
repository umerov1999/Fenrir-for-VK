package dev.ragnarok.fenrir.module.hls

import dev.ragnarok.fenrir.module.FenrirNative.isNativeLoaded

object TSDemuxer {
    private external fun unpack(
        input: String,
        output: String
    ): Int

    fun unpackNative(input: String, output: String): Int {
        if (!isNativeLoaded) {
            return 0
        }
        return unpack(input, output)
    }
}