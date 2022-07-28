package dev.ragnarok.fenrir.mvp.presenter

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.annotation.StringRes
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.model.SideDrawerCategory
import dev.ragnarok.fenrir.model.SideSwitchableCategory
import dev.ragnarok.fenrir.mvp.core.AbsPresenter
import dev.ragnarok.fenrir.mvp.view.ISideDrawerEditView
import dev.ragnarok.fenrir.settings.Settings
import java.util.*

class SideDrawerEditPresenter(savedInstanceState: Bundle?) :
    AbsPresenter<ISideDrawerEditView>(savedInstanceState) {
    private val data: List<SideDrawerCategory>
    private fun createInitialData(): ArrayList<SideDrawerCategory> {
        val categories = ArrayList<SideDrawerCategory>()
        val settings = Settings.get().sideDrawerSettings()
        @SideSwitchableCategory val items = settings.categoriesOrder
        for (category in items) {
            val c = SideDrawerCategory(category, getTitleResCategory(category))
            c.setChecked(settings.isCategoryEnabled(category))
            categories.add(c)
        }
        return categories
    }

    override fun onGuiCreated(viewHost: ISideDrawerEditView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(data)
    }

    @SuppressLint("WrongConstant")
    private fun save() {
        @SideSwitchableCategory val postions = IntArray(data.size)
        val active = BooleanArray(data.size)
        for (i in data.indices) {
            val category = data[i]
            postions[i] = category.getKey()
            active[i] = category.isChecked()
        }
        Settings.get().sideDrawerSettings().setCategoriesOrder(postions, active)
    }

    fun fireSaveClick() {
        save()
        view?.goBackAndApplyChanges()
    }

    fun fireItemMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(data, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(data, i, i - 1)
            }
        }
    }

    companion object {
        @StringRes
        internal fun getTitleResCategory(@SideSwitchableCategory type: Int): Int {
            when (type) {
                SideSwitchableCategory.FRIENDS -> return R.string.friends
                SideSwitchableCategory.NEWSFEED_COMMENTS -> return R.string.drawer_newsfeed_comments
                SideSwitchableCategory.GROUPS -> return R.string.groups
                SideSwitchableCategory.PHOTOS -> return R.string.photos
                SideSwitchableCategory.VIDEOS -> return R.string.videos
                SideSwitchableCategory.MUSIC -> return R.string.music
                SideSwitchableCategory.DOCS -> return R.string.documents
                SideSwitchableCategory.BOOKMARKS -> return R.string.bookmarks
                SideSwitchableCategory.DIALOGS -> return R.string.dialogs
                SideSwitchableCategory.FEED -> return R.string.feed
                SideSwitchableCategory.FEEDBACK -> return R.string.drawer_feedback
                SideSwitchableCategory.SEARCH -> return R.string.search
            }
            throw IllegalArgumentException()
        }
    }

    init {
        data = createInitialData()
    }
}