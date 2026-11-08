package dev.ragnarok.fenrir.module

import androidx.annotation.Keep

object FileUtils {
    private external fun getThreadsCountNative(): Int
    private external fun listDirRecursiveNative(dir: String, listener: ListDirRecursive)
    private external fun listDirRecursiveNativePointer(dir: String, pointer: Long)
    private external fun audioTagStripNative(audioFile: String): Boolean
    private external fun videoTagStripNative(file: String): Boolean
    private external fun audioTagModifyNative(
        audioFile: String,
        coverFile: String?,
        coverMimeType: String?,
        title: String?,
        artist: String?,
        albumTitle: String?,
        ifGenre: String?,
        comment: String?
    ): Boolean

    private external fun videoTagModifyNative(
        file: String,
        coverFile: String?,
        title: String?,
        artist: String?,
        albumTitle: String?,
        ifGenre: String?,
        comment: String?
    ): Boolean

    fun audioTagStrip(audioFile: String): Boolean {
        return audioTagStripNative(audioFile)
    }

    fun videoTagStrip(file: String): Boolean {
        return videoTagStripNative(file)
    }

    fun audioTagModify(
        audioFile: String,
        coverFile: String?,
        coverMimeType: String?,
        title: String?,
        artist: String?,
        albumTitle: String?,
        ifGenre: String?,
        comment: String?
    ): Boolean {
        return audioTagModifyNative(
            audioFile,
            coverFile,
            coverMimeType,
            title,
            artist,
            albumTitle,
            ifGenre,
            comment
        )
    }

    fun videoTagModify(
        file: String,
        coverFile: String?,
        title: String?,
        artist: String?,
        albumTitle: String?,
        ifGenre: String?,
        comment: String?
    ): Boolean {
        return videoTagModifyNative(
            file,
            coverFile,
            title,
            artist,
            albumTitle,
            ifGenre,
            comment
        )
    }

    fun listDirRecursive(dir: String, listener: ListDirRecursive) {
        return listDirRecursiveNative(dir, listener)
    }

    fun listDirRecursive(dir: String, pointer: Long) {
        return listDirRecursiveNativePointer(dir, pointer)
    }

    val threadsCount: Int
        get() = getThreadsCountNative()

    @Keep
    interface ListDirRecursive {
        fun onEntry(file: String)
    }
}