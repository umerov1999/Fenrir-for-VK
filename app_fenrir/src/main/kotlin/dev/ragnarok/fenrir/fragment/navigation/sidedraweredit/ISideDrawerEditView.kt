package dev.ragnarok.fenrir.fragment.navigation.sidedraweredit

import dev.ragnarok.fenrir.fragment.base.core.IMvpView
import dev.ragnarok.fenrir.model.SideDrawerCategory

interface ISideDrawerEditView : IMvpView {
    fun displayData(data: List<SideDrawerCategory>)
    fun goBackAndApplyChanges()
}