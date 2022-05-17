package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.LocalPhoto
import dev.ragnarok.fenrir.mvp.core.IMvpView

interface ILocalPhotosView : IMvpView, IErrorView {
    fun displayData(data: List<LocalPhoto>)
    fun setEmptyTextVisible(visible: Boolean)
    fun displayProgress(loading: Boolean)
    fun returnResultToParent(photos: ArrayList<LocalPhoto>)
    fun updateSelectionAndIndexes()
    fun setFabVisible(visible: Boolean, anim: Boolean)
    fun requestReadExternalStoragePermission()
}