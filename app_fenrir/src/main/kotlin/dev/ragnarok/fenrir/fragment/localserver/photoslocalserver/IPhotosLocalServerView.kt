package dev.ragnarok.fenrir.fragment.localserver.photoslocalserver

import dev.ragnarok.fenrir.fragment.base.IAccountDependencyView
import dev.ragnarok.fenrir.fragment.base.core.IErrorView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.model.TmpSource

interface IPhotosLocalServerView : IMvpView, IErrorView, IAccountDependencyView {
    fun displayList(photos: List<Photo>)
    fun notifyListChanged()
    fun notifyItemChanged(index: Int)
    fun notifyDataAdded(position: Int, count: Int)
    fun displayLoading(loading: Boolean)
    fun displayGallery(
        accountId: Int,
        albumId: Int,
        ownerId: Int,
        source: TmpSource,
        position: Int,
        reversed: Boolean
    )

    fun displayGalleryUnSafe(
        accountId: Int,
        albumId: Int,
        ownerId: Int,
        parcelNativePointer: Long,
        position: Int,
        reversed: Boolean
    )

    fun scrollTo(position: Int)
}