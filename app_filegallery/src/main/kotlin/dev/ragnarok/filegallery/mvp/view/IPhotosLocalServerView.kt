package dev.ragnarok.filegallery.mvp.view

import dev.ragnarok.filegallery.model.Photo
import dev.ragnarok.filegallery.mvp.core.IMvpView

interface IPhotosLocalServerView : IMvpView, IErrorView {
    fun displayList(photos: List<Photo>)
    fun notifyListChanged()
    fun notifyItemChanged(index: Int)
    fun notifyDataAdded(position: Int, count: Int)
    fun displayLoading(loading: Boolean)
    fun displayGalleryUnSafe(parcelNativePointer: Long, position: Int, reversed: Boolean)
    fun scrollTo(position: Int)
}