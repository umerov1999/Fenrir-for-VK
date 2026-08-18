package dev.ragnarok.fenrir.activity

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.insets.ProtectionLayout
import androidx.core.view.iterator
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.applyAlpha
import dev.ragnarok.fenrir.fragment.PreferencesFragment.Companion.newInstance
import dev.ragnarok.fenrir.fragment.SecurityPreferencesFragment
import dev.ragnarok.fenrir.fragment.accounts.AccountsFragment
import dev.ragnarok.fenrir.fragment.shortcutsview.ShortcutsViewFragment
import dev.ragnarok.fenrir.fragment.theme.ThemeFragment
import dev.ragnarok.fenrir.place.Place
import dev.ragnarok.fenrir.place.PlaceProvider
import dev.ragnarok.fenrir.settings.CurrentTheme.getNavigationBarColor
import dev.ragnarok.fenrir.settings.CurrentTheme.getStatusBarColor
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.toast.CustomSnackbars

class AccountsActivity : NoMainActivity(), PlaceProvider {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(noMainContainerViewId, AccountsFragment())
                .addToBackStack("accounts")
                .commit()
        }

        val statusBarColor = getStatusBarColor(this)
        val navigationBarColor = getNavigationBarColor(this)
        val invertIcons = !Settings.get().ui().isDarkModeEnabled(this)
        val statusBarStyle = if (invertIcons) SystemBarStyle.light(
            statusBarColor.applyAlpha(180),
            statusBarColor.applyAlpha(180)
        ) else SystemBarStyle.dark(statusBarColor.applyAlpha(180))
        val navigationBarStyle = if (invertIcons) SystemBarStyle.light(
            navigationBarColor.applyAlpha(180),
            navigationBarColor.applyAlpha(180)
        ) else SystemBarStyle.dark(navigationBarColor.applyAlpha(180))

        for (i in (window.decorView as ViewGroup)) {
            if (i is ProtectionLayout) {
                (window.decorView as ViewGroup).removeView(i)
            }
        }
        enableEdgeToEdge(statusBarStyle, navigationBarStyle)
    }

    override fun openPlace(place: Place) {
        when (place.type) {
            Place.PREFERENCES -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(noMainContainerViewId, newInstance(place.safeArguments()))
                    .addToBackStack("preferences")
                    .commit()
            }

            Place.SETTINGS_THEME -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(noMainContainerViewId, ThemeFragment())
                    .addToBackStack("preferences_themes")
                    .commit()
            }

            Place.SECURITY -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(noMainContainerViewId, SecurityPreferencesFragment())
                    .addToBackStack("security")
                    .commit()
            }

            Place.SHORTCUTS -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(noMainContainerViewId, ShortcutsViewFragment())
                    .addToBackStack("shortcuts")
                    .commit()
            }

            else -> {
                CustomSnackbars.createCustomSnackbars(findViewById(noMainContainerViewId))
                    ?.coloredSnack(R.string.not_available, Color.RED, false)?.show()
            }
        }
    }
}