package dev.ragnarok.filegallery.mvp.view

import dev.ragnarok.filegallery.model.Photo
import dev.ragnarok.filegallery.model.Video
import dev.ragnarok.filegallery.mvp.core.IMvpView

interface IPhotoPagerView : IMvpView, IErrorView, IToastView {
    fun displayPhotos(photos: List<Photo>, initialIndex: Int)
    fun setToolbarTitle(title: String?)
    fun setToolbarSubtitle(subtitle: String?)
    fun displayPhotoListLoading(loading: Boolean)
    fun setButtonsBarVisible(visible: Boolean)
    fun setToolbarVisible(visible: Boolean)
    fun closeOnly()
    fun returnInfo(position: Int, parcelNativePtr: Long)
    fun returnOnlyPos(position: Int)
    fun returnFileInfo(path: String)
    fun displayVideo(video: Video)
}