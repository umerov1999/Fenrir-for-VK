package dev.ragnarok.fenrir.activity;

import android.os.Bundle;

import dev.ragnarok.fenrir.fragment.AccountsFragment;
import dev.ragnarok.fenrir.fragment.PreferencesFragment;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceProvider;

public class AccountsActivity extends NoMainActivity implements PlaceProvider {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(getMainContainerViewId(), new AccountsFragment())
                    .addToBackStack("accounts")
                    .commit();
        }
    }

    @Override
    public void openPlace(Place place) {
        if (place.getType() == Place.PREFERENCES) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(getMainContainerViewId(), PreferencesFragment.newInstance(place.getArgs()))
                    .addToBackStack("preferences")
                    .commit();
        }
    }

}