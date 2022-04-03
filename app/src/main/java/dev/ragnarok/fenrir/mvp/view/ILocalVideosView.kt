package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.LocalVideo
import dev.ragnarok.fenrir.mvp.core.IMvpView

interface ILocalVideosView : IMvpView, IErrorView {
    fun displayData(data: List<LocalVideo>)
    fun setEmptyTextVisible(visible: Boolean)
    fun displayProgress(loading: Boolean)
    fun returnResultToParent(videos: ArrayList<LocalVideo>)
    fun updateSelectionAndIndexes()
    fun setFabVisible(visible: Boolean, anim: Boolean)
}