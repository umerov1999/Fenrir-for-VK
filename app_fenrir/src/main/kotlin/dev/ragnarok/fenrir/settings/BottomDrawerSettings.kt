package dev.ragnarok.fenrir.settings

import android.content.Context
import androidx.core.content.edit
import de.maxr1998.modernpreferences.PreferenceScreen
import dev.ragnarok.fenrir.kJson
import dev.ragnarok.fenrir.model.DrawerCategory
import dev.ragnarok.fenrir.model.SwitchableCategory
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.createPublishSubject
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.myEmit
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.builtins.ListSerializer

internal class BottomDrawerSettings(context: Context) : ISettings.IDrawerSettings {
    private val app: Context = context.applicationContext
    internal fun makeDefaults(): List<DrawerCategory> {
        return listOf(
            DrawerCategory(SwitchableCategory.FEED),
            DrawerCategory(SwitchableCategory.SEARCH),
            DrawerCategory(SwitchableCategory.DIALOGS),
            DrawerCategory(SwitchableCategory.FEEDBACK),
            DrawerCategory(SwitchableCategory.FRIENDS, false),
            DrawerCategory(SwitchableCategory.STORIES, false),
            DrawerCategory(SwitchableCategory.CLIPS, false),
            DrawerCategory(SwitchableCategory.BIRTHDAYS, false),
            DrawerCategory(SwitchableCategory.GROUPS, false),
            DrawerCategory(SwitchableCategory.PHOTOS, false),
            DrawerCategory(SwitchableCategory.VIDEOS, false),
            DrawerCategory(SwitchableCategory.MUSIC, false),
            DrawerCategory(SwitchableCategory.DOCS, false),
            DrawerCategory(SwitchableCategory.FAVES, false),
            DrawerCategory(SwitchableCategory.SETTINGS, false),
            DrawerCategory(SwitchableCategory.ACCOUNTS, false)
        )
    }

    override var categoriesOrder: List<DrawerCategory>
        get() {
            val defaults = makeDefaults()
            val jsonString =
                PreferenceScreen.getPreferences(app).getString("bottom_navigation_menu_order", null)
                    ?: return makeDefaults()

            return try {
                val data =
                    kJson.decodeFromString(ListSerializer(DrawerCategory.serializer()), jsonString)
                var needClear = false
                for (i in data) {
                    var has = false
                    for (s in defaults) {
                        if (s.id == i.id) {
                            has = true
                            break
                        }
                    }
                    if (!has) {
                        needClear = true
                        break
                    }
                }
                for (i in defaults) {
                    var has = false
                    for (s in data) {
                        if (s.id == i.id) {
                            has = true
                            break
                        }
                    }
                    if (!has) {
                        needClear = true
                        break
                    }
                }
                if (needClear) {
                    throw UnsupportedOperationException()
                }
                data
            } catch (_: Exception) {
                PreferenceScreen.getPreferences(app).edit {
                    putString(
                        "bottom_navigation_menu_order",
                        kJson.encodeToString(
                            ListSerializer(DrawerCategory.serializer()),
                            defaults
                        )
                    )
                }
                defaults
            }
        }
        set(list) {
            PreferenceScreen.getPreferences(app).edit {
                putString(
                    "bottom_navigation_menu_order",
                    kJson.encodeToString(ListSerializer(DrawerCategory.serializer()), list)
                )
            }
            observeChanges.myEmit(list)
        }

    override val observeChanges: SharedFlow<List<DrawerCategory>>
        field = createPublishSubject<List<DrawerCategory>>()

    override fun reset() {
        PreferenceScreen.getPreferences(app).edit {
            remove(
                "bottom_navigation_menu_order"
            )
        }
        observeChanges.myEmit(makeDefaults())
    }
}
