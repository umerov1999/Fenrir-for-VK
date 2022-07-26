package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.ShortcutStored
import dev.ragnarok.fenrir.mvp.core.IMvpView

interface IShortcutsView : IMvpView, IErrorView {
    fun displayData(shortcuts: List<ShortcutStored>)
    fun notifyItemRemoved(position: Int)
    fun notifyDataSetChanged()
}
