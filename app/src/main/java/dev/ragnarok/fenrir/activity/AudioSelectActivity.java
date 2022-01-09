package dev.ragnarok.fenrir.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.fragment.AudioSelectTabsFragment;
import dev.ragnarok.fenrir.fragment.AudiosFragment;
import dev.ragnarok.fenrir.fragment.search.SingleTabSearchFragment;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceProvider;
import dev.ragnarok.fenrir.util.Objects;

public class AudioSelectActivity extends NoMainActivity implements PlaceProvider {

    /**
     * @param accountId От чьего имени получать
     */
    public static Intent createIntent(Context context, int accountId) {
        return new Intent(context, AudioSelectActivity.class)
                .putExtra(Extra.ACCOUNT_ID, accountId);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Objects.isNull(savedInstanceState)) {
            int accountId = getIntent().getExtras().getInt(Extra.ACCOUNT_ID);
            attachInitialFragment(accountId);
        }
    }

    private void attachInitialFragment(int accountId) {
        AudioSelectTabsFragment fragment = AudioSelectTabsFragment.newInstance(accountId);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                .replace(getMainContainerViewId(), fragment)
                .addToBackStack("audio-select")
                .commit();
    }

    @Override
    public void openPlace(Place place) {
        if (place.getType() == Place.SINGLE_SEARCH) {
            SingleTabSearchFragment singleTabSearchFragment = SingleTabSearchFragment.newInstance(place.getArgs());
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                    .replace(getMainContainerViewId(), singleTabSearchFragment)
                    .addToBackStack("audio-search-select")
                    .commit();
        } else if (place.getType() == Place.AUDIOS_IN_ALBUM) {
            Bundle args = place.getArgs();
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                    .replace(getMainContainerViewId(), AudiosFragment.newInstance(args, true))
                    .addToBackStack("audio-in_playlist-select")
                    .commit();
        }
    }
}
