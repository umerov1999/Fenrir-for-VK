package dev.ragnarok.fenrir.settings

import android.content.Context
import de.maxr1998.modernpreferences.PreferenceScreen.Companion.getPreferences
import dev.ragnarok.fenrir.model.SwitchableCategory
import dev.ragnarok.fenrir.settings.ISettings.IDrawerSettings
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

internal class DrawerSettings(context: Context) : IDrawerSettings {
    private val app: Context = context.applicationContext
    private val publishSubject: PublishSubject<Any> = PublishSubject.create()
    override fun isCategoryEnabled(@SwitchableCategory category: Int): Boolean {
        return getPreferences(app)
            .getBoolean(keyForCategory(category), true)
    }

    override fun setCategoriesOrder(@SwitchableCategory order: IntArray, active: BooleanArray) {
        val preferences = getPreferences(app)
        for (i in order.indices) {
            @SwitchableCategory val category = order[i]
            preferences.edit()
                .putBoolean(keyForCategory(category), active[i])
                .apply()
        }
        val builder = StringBuilder()
        for (i in order) {
            builder.append(i).append("-")
        }
        preferences.edit()
            .putString("drawer_categories_order", builder.toString().trim { it <= ' ' })
            .apply()
        publishSubject.onNext(Void::class.java)
    }/*ignore*/// категория "category" должна быть в положении "i"

    /*ignore*/
    override val categoriesOrder: IntArray
        get() {
            @SwitchableCategory val all = intArrayOf(
                SwitchableCategory.FRIENDS,
                SwitchableCategory.NEWSFEED_COMMENTS,
                SwitchableCategory.GROUPS,
                SwitchableCategory.PHOTOS,
                SwitchableCategory.VIDEOS,
                SwitchableCategory.MUSIC,
                SwitchableCategory.DOCS,
                SwitchableCategory.BOOKMARKS
            )
            val line = getPreferences(app).getString("drawer_categories_order", null)
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
        internal fun findCategoryIndex(array: IntArray, @SwitchableCategory category: Int): Int {
            for (i in array.indices) {
                if (array[i] == category) {
                    return i
                }
            }
            throw IllegalStateException("Invalid category $category")
        }

        internal fun keyForCategory(@SwitchableCategory category: Int): String {
            return "drawer_category_$category"
        }
    }

}