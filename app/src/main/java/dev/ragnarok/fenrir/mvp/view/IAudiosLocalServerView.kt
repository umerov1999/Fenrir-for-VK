package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IAudiosLocalServerView : IMvpView, IErrorView, IAccountDependencyView {
    fun displayList(audios: List<Audio>)
    fun notifyListChanged()
    fun notifyItemChanged(index: Int)
    fun notifyDataAdded(position: Int, count: Int)
    fun displayLoading(loading: Boolean)
    fun displayOptionsDialog(isReverse: Boolean, isDiscography: Boolean)
}