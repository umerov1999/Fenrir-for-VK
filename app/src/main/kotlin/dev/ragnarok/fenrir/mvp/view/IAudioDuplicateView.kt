package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.mvp.core.IMvpView

interface IAudioDuplicateView : IMvpView, IErrorView {
    fun displayData(new_audio: Audio, old_audio: Audio)
    fun setOldBitrate(bitrate: Int?)
    fun setNewBitrate(bitrate: Int?)
    fun updateShowBitrate(needShow: Boolean)
}