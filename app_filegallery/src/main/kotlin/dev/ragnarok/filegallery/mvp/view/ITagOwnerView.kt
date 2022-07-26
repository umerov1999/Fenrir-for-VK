package dev.ragnarok.filegallery.mvp.view

import dev.ragnarok.filegallery.model.FileItem
import dev.ragnarok.filegallery.model.tags.TagOwner
import dev.ragnarok.filegallery.mvp.core.IMvpView

interface ITagOwnerView : IMvpView, IErrorView, IToastView {
    fun displayData(data: List<TagOwner>)
    fun notifyChanges()
    fun notifyAdd(index: Int)
    fun notifyRemove(index: Int)
    fun successAdd(owner: TagOwner, item: FileItem)
}