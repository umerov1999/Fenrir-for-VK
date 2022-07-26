package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.LocalImageAlbum
import dev.ragnarok.fenrir.mvp.core.IMvpView

interface ILocalPhotoAlbumsView : IMvpView {
    fun displayData(data: List<LocalImageAlbum>)
    fun setEmptyTextVisible(visible: Boolean)
    fun displayProgress(loading: Boolean)
    fun openAlbum(album: LocalImageAlbum)
    fun notifyDataChanged()
    fun requestReadExternalStoragePermission()
}