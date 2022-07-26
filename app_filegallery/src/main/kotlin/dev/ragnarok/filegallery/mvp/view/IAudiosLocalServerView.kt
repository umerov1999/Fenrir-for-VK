package dev.ragnarok.filegallery.mvp.view

import dev.ragnarok.filegallery.model.Audio
import dev.ragnarok.filegallery.mvp.core.IMvpView

interface IAudiosLocalServerView : IMvpView, IErrorView {
    fun displayList(audios: List<Audio>)
    fun notifyListChanged()
    fun notifyItemChanged(index: Int)
    fun notifyDataAdded(position: Int, count: Int)
    fun displayLoading(loading: Boolean)
    fun displayOptionsDialog(isReverse: Boolean, isDiscography: Boolean)
}