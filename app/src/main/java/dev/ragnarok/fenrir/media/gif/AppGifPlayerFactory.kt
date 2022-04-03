package dev.ragnarok.fenrir.media.gif

import dev.ragnarok.fenrir.settings.IProxySettings

class AppGifPlayerFactory(private val proxySettings: IProxySettings) : IGifPlayerFactory {
    override fun createGifPlayer(url: String, isRepeat: Boolean): IGifPlayer {
        val config = proxySettings.activeProxy
        return ExoGifPlayer(url, config, isRepeat)
    }
}