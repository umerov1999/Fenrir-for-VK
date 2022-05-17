package dev.ragnarok.fenrir.mvp.view.search

import dev.ragnarok.fenrir.model.Photo

interface IPhotoSearchView : IBaseSearchView<Photo> {
    fun displayGallery(accountId: Int, photos: ArrayList<Photo>, position: Int)
}