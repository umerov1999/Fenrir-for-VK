package dev.ragnarok.fenrir.fragment.navigationedit

import dev.ragnarok.fenrir.fragment.base.core.IErrorView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView
import dev.ragnarok.fenrir.model.DrawerCategory

interface IDrawerEditView : IMvpView, IErrorView {
    fun displayData(data: List<DrawerCategory>)
    fun goBackAndApplyChanges()
    fun notifyDataSetChanged()
}