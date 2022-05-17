package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.SideDrawerCategory
import dev.ragnarok.fenrir.mvp.core.IMvpView

interface ISideDrawerEditView : IMvpView {
    fun displayData(data: List<SideDrawerCategory>)
    fun goBackAndApplyChanges()
}