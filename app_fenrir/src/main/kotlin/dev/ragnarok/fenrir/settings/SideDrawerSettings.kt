package dev.ragnarok.fenrir.settings

import android.content.Context
import de.maxr1998.modernpreferences.PreferenceScreen.Companion.getPreferences
import dev.ragnarok.fenrir.model.SideSwitchableCategory
import dev.ragnarok.fenrir.settings.ISettings.ISideDrawerSettings
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

internal class SideDrawerSettings(context: Context) : ISideDrawerSettings {
    private val app: Context = context.applicationContext
    private val publishSubject: PublishSubject<Any> = PublishSubject.create()
    override fun isCategoryEnabled(@SideSwitchableCategory category: Int): Boolean {
        return getPreferences(app)
            .getBoolean(keyForCategory(category), true)
    }

    override fun setCategoriesOrder(@SideSwitchableCategory order: IntArray, active: BooleanArray) {
        val preferences = getPreferences(app)
        for (i in order.indices) {
            @SideSwitchableCategory val category = order[i]
            preferences.edit()
                .putBoolean(keyForCategory(category), active[i])
                .apply()
        }
        val builder = StringBuilder()
        for (i in order) {
            builder.append(i).append("-")
        }
        preferences.edit()
            .putString("side_drawer_categories_order", builder.toString().trim { it <= ' ' })
            .apply()
        publishSubject.onNext(Void::class.java)
    }/*ignore*/// категория "category" должна быть в положении "i"

    /*ignore*/
    override val categoriesOrder: IntArray
        get() {
            @SideSwitchableCategory val all = intArrayOf(
                SideSwitchableCategory.FRIENDS,
                SideSwitchableCategory.DIALOGS,
                SideSwitchableCategory.FEED,
                SideSwitchableCategory.FEEDBACK,
                SideSwitchableCategory.NEWSFEED_COMMENTS,
                SideSwitchableCategory.GROUPS,
                SideSwitchableCategory.PHOTOS,
                SideSwitchableCategory.VIDEOS,
                SideSwitchableCategory.MUSIC,
                SideSwitchableCategory.DOCS,
                SideSwitchableCategory.BOOKMARKS,
                SideSwitchableCategory.SEARCH
            )
            val line = getPreferences(app).getString("side_drawer_categories_order", null)
            val parts: Array<String> =
                if (line.isNullOrEmpty()) emptyArray() else line.split(Regex("-"))
                    .toTypedArray()
            val positions = IntArray(parts.size)
            try {
                for (i in parts.indices) {
                    positions[i] = parts[i].toInt()
                }
            } catch (ignored: Exception) { /*ignore*/
            }
            for (i in positions.indices) {
                val category = positions[i]
                // категория "category" должна быть в положении "i"
                if (i >= all.size) break
                if (all[i] != category) {
                    try {
                        val currentCategoryIndex = findCategoryIndex(all, category)
                        all[currentCategoryIndex] = all[i]
                        all[i] = category
                    } catch (ignored: Exception) { /*ignore*/
                    }
                }
            }
            return all
        }

    override fun observeChanges(): Observable<Any> {
        return publishSubject
    }

    companion object {
        private fun findCategoryIndex(array: IntArray, @SideSwitchableCategory category: Int): Int {
            for (i in array.indices) {
                if (array[i] == category) {
                    return i
                }
            }
            throw IllegalStateException("Invalid category $category")
        }

        private fun keyForCategory(@SideSwitchableCategory category: Int): String {
            return "side_drawer_category_$category"
        }
    }

}