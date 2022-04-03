package dev.ragnarok.fenrir.mvp.view.search

import dev.ragnarok.fenrir.model.Audio

interface IAudioSearchView : IBaseSearchView<Audio> {
    fun notifyAudioChanged(index: Int)
}