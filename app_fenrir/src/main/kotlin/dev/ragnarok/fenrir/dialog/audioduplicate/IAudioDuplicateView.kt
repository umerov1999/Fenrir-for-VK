package dev.ragnarok.fenrir.dialog.audioduplicate

import dev.ragnarok.fenrir.fragment.base.core.IErrorView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView
import dev.ragnarok.fenrir.model.Audio

interface IAudioDuplicateView : IMvpView, IErrorView {
    fun displayData(newAudio: Audio, oldAudio: Audio)
    fun setOldBitrate(bitrate: Int?)
    fun setNewBitrate(bitrate: Int?)
    fun updateShowBitrate(needShow: Boolean)
}