package dev.ragnarok.fenrir.mvp.presenter

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.annotation.StringRes
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.model.DrawerCategory
import dev.ragnarok.fenrir.model.SwitchableCategory
import dev.ragnarok.fenrir.mvp.core.AbsPresenter
import dev.ragnarok.fenrir.mvp.view.IDrawerEditView
import dev.ragnarok.fenrir.settings.Settings
import java.util.*

class DrawerEditPresenter(savedInstanceState: Bundle?) :
    AbsPresenter<IDrawerEditView>(savedInstanceState) {
    private val data: List<DrawerCategory>
    private fun createInitialData(): ArrayList<DrawerCategory> {
        val categories = ArrayList<DrawerCategory>()
        val settings = Settings.get().drawerSettings()
        @SwitchableCategory val items = settings.categoriesOrder
        for (category in items) {
            val c = DrawerCategory(category, getTitleResCategory(category))
            c.isChecked = settings.isCategoryEnabled(category)
            categories.add(c)
        }
        return categories
    }

    override fun onGuiCreated(viewHost: IDrawerEditView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(data)
    }

    @SuppressLint("WrongConstant")
    private fun save() {
        @SwitchableCategory val postions = IntArray(data.size)
        val active = BooleanArray(data.size)
        for (i in data.indices) {
            val category = data[i]
            postions[i] = category.key
            active[i] = category.isChecked
        }
        Settings.get().drawerSettings().setCategoriesOrder(postions, active)
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
        private fun getTitleResCategory(@SwitchableCategory type: Int): Int {
            when (type) {
                SwitchableCategory.FRIENDS -> return R.string.friends
                SwitchableCategory.NEWSFEED_COMMENTS -> return R.string.drawer_newsfeed_comments
                SwitchableCategory.GROUPS -> return R.string.groups
                SwitchableCategory.PHOTOS -> return R.string.photos
                SwitchableCategory.VIDEOS -> return R.string.videos
                SwitchableCategory.MUSIC -> return R.string.music
                SwitchableCategory.DOCS -> return R.string.documents
                SwitchableCategory.BOOKMARKS -> return R.string.bookmarks
            }
            throw IllegalArgumentException()
        }
    }

    init {
        data = createInitialData()
    }
}