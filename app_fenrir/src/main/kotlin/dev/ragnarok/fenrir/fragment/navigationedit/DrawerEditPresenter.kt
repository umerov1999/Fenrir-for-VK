package dev.ragnarok.fenrir.fragment.navigationedit

import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.fragment.base.core.AbsPresenter
import dev.ragnarok.fenrir.model.DrawerCategory
import dev.ragnarok.fenrir.model.DrawerType
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.swap

class DrawerEditPresenter(@DrawerType private val drawerType: Int) :
    AbsPresenter<IDrawerEditView>() {
    private var data: ArrayList<DrawerCategory> =
        ArrayList(
            when (drawerType) {
                DrawerType.BOTTOM -> Settings.get().bottomDrawerSettings().categoriesOrder
                DrawerType.SIDE -> Settings.get().sideDrawerSettings().categoriesOrder
                else -> Settings.get().drawerSettings().categoriesOrder
            }
        )


    override fun onGuiCreated(viewHost: IDrawerEditView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(data)
    }

    private fun save(): Boolean {
        return when (drawerType) {
            DrawerType.BOTTOM -> {
                var actives = 0
                for (i in data) {
                    if (i.active) {
                        actives++
                    }
                }
                if (actives > 5) {
                    view?.showError(R.string.bottom_navigation_menu_max_5)
                    false
                } else {
                    Settings.get().bottomDrawerSettings().categoriesOrder = data
                    true
                }
            }

            DrawerType.SIDE -> {
                Settings.get().sideDrawerSettings().categoriesOrder = data
                true
            }

            else -> {
                Settings.get().drawerSettings().categoriesOrder = data
                true
            }
        }
    }

    fun fireResetClick() {
        when (drawerType) {
            DrawerType.BOTTOM -> {
                Settings.get().bottomDrawerSettings().reset()
                data = ArrayList(Settings.get().bottomDrawerSettings().categoriesOrder)
            }

            DrawerType.SIDE -> {
                Settings.get().sideDrawerSettings().reset()
                data = ArrayList(Settings.get().sideDrawerSettings().categoriesOrder)
            }

            else -> {
                Settings.get().drawerSettings().reset()
                data = ArrayList(Settings.get().drawerSettings().categoriesOrder)
            }
        }
        view?.displayData(data)
    }

    fun fireSaveClick() {
        if (save()) {
            view?.goBackAndApplyChanges()
        }
    }

    fun fireItemMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                data.swap(i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                data.swap(i, i - 1)
            }
        }
    }

}