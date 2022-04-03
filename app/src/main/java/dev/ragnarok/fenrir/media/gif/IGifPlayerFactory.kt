package dev.ragnarok.fenrir.media.gif

interface IGifPlayerFactory {
    fun createGifPlayer(url: String, isRepeat: Boolean): IGifPlayer
}