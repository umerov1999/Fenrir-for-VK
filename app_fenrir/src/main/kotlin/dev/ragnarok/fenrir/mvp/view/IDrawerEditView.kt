package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.DrawerCategory
import dev.ragnarok.fenrir.mvp.core.IMvpView

interface IDrawerEditView : IMvpView {
    fun displayData(data: List<DrawerCategory>)
    fun goBackAndApplyChanges()
}