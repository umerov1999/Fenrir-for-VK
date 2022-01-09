package dev.ragnarok.fenrir.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.slidr.Slidr;
import dev.ragnarok.fenrir.activity.slidr.model.SlidrConfig;
import dev.ragnarok.fenrir.activity.slidr.model.SlidrPosition;
import dev.ragnarok.fenrir.fragment.AudioPlayerFragment;
import dev.ragnarok.fenrir.fragment.GifPagerFragment;
import dev.ragnarok.fenrir.fragment.SinglePhotoFragment;
import dev.ragnarok.fenrir.listener.AppStyleable;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceProvider;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;


public class PhotoFullScreenActivity extends NoMainActivity implements PlaceProvider, AppStyleable {
    public static final String ACTION_OPEN_PLACE = "dev.ragnarok.fenrir.activity.PhotoFullScreenActivity.openPlace";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Slidr.attach(this, new SlidrConfig.Builder().fromUnColoredToColoredStatusBar(true).position(SlidrPosition.VERTICAL).scrimColor(CurrentTheme.getColorBackground(this)).build());
        if (Objects.isNull(savedInstanceState)) {
            handleIntent(getIntent());
        }
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            finish();
            return;
        }
        String action = intent.getAction();
        if (ACTION_OPEN_PLACE.equals(action)) {
            Place place = intent.getParcelableExtra(Extra.PLACE);
            if (Objects.isNull(place)) {
                finish();
                return;
            }
            openPlace(place);
        }
    }

    @Override
    public void openPlace(Place place) {
        Bundle args = place.getArgs();
        switch (place.getType()) {
            case Place.SINGLE_PHOTO:
                attachToFront(SinglePhotoFragment.newInstance(args));
                break;

            case Place.GIF_PAGER:
                attachToFront(GifPagerFragment.newInstance(args));
                break;
            case Place.PLAYER:
                Fragment player = getSupportFragmentManager().findFragmentByTag("audio_player");
                if (player instanceof AudioPlayerFragment)
                    ((AudioPlayerFragment) player).dismiss();
                AudioPlayerFragment.newInstance(args).show(getSupportFragmentManager(), "audio_player");
                break;
            default:
                Utils.openPlaceWithSwipebleActivity(this, place);
                break;
        }
    }

    private void attachToFront(Fragment fragment) {
        attachToFront(fragment, true);
    }

    private void attachToFront(Fragment fragment, boolean animate) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (animate)
            fragmentTransaction.setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit);

        fragmentTransaction
                .replace(R.id.fragment, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    @Override
    public void hideMenu(boolean hide) {

    }

    @Override
    public void openMenu(boolean open) {

    }

    @Override
    public void setStatusbarColored(boolean colored, boolean invertIcons) {
        int statusbarNonColored = CurrentTheme.getStatusBarNonColored(this);
        int statusbarColored = CurrentTheme.getStatusBarColor(this);

        Window w = getWindow();
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setStatusBarColor(colored ? statusbarColored : statusbarNonColored);
        @ColorInt
        int navigationColor = colored ? CurrentTheme.getNavigationBarColor(this) : Color.BLACK;
        w.setNavigationBarColor(navigationColor);

        if (Utils.hasMarshmallow()) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            if (invertIcons) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }

        if (Utils.hasOreo()) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            if (invertIcons) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                w.getDecorView().setSystemUiVisibility(flags);
                w.setNavigationBarColor(Color.WHITE);
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                w.getDecorView().setSystemUiVisibility(flags);
            }
        }
    }
}
