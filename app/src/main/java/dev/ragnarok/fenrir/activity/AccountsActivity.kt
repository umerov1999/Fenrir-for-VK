package dev.ragnarok.fenrir.activity

import android.graphics.Color
import android.os.Bundle
import com.google.android.material.snackbar.BaseTransientBottomBar
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.fragment.AccountsFragment
import dev.ragnarok.fenrir.fragment.PreferencesFragment.Companion.newInstance
import dev.ragnarok.fenrir.fragment.ThemeFragment
import dev.ragnarok.fenrir.place.Place
import dev.ragnarok.fenrir.place.PlaceProvider
import dev.ragnarok.fenrir.util.Utils

class AccountsActivity : NoMainActivity(), PlaceProvider {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(getMainContainerViewId(), AccountsFragment())
                .addToBackStack("accounts")
                .commit()
        }
    }

    override fun openPlace(place: Place) {
        when (place.type) {
            Place.PREFERENCES -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(getMainContainerViewId(), newInstance(place.safeArguments()))
                    .addToBackStack("preferences")
                    .commit()
            }
            Place.SETTINGS_THEME -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(getMainContainerViewId(), ThemeFragment())
                    .addToBackStack("preferences_themes")
                    .commit()
            }
            else -> {
                Utils.ColoredSnack(
                    findViewById(
                        getMainContainerViewId()
                    ), R.string.not_available, BaseTransientBottomBar.LENGTH_SHORT, Color.RED
                ).show()
            }
        }
    }
}