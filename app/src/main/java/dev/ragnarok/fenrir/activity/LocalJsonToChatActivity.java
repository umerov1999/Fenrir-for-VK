package dev.ragnarok.fenrir.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.Collections;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.fragment.AudioPlayerFragment;
import dev.ragnarok.fenrir.fragment.LocalJsonToChatFragment;
import dev.ragnarok.fenrir.listener.AppStyleable;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.place.PlaceProvider;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public class LocalJsonToChatActivity extends NoMainActivity implements PlaceProvider, AppStyleable {

    private final FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener = this::keyboardHide;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Objects.isNull(savedInstanceState)) {
            handleIntent(getIntent());
            getSupportFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener);
        }
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            finish();
            return;
        }
        int accountId = Settings.get().accounts().getCurrent();
        if (accountId == ISettings.IAccountsSettings.INVALID_ID) {
            finish();
            return;
        }
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            attachInitialFragment(LocalJsonToChatFragment.newInstance(accountId));
        }
    }

    public void keyboardHide() {
        try {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception ignored) {
        }
    }

    private void attachInitialFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                .replace(getMainContainerViewId(), fragment)
                .addToBackStack("primary_local_chat")
                .commitAllowingStateLoss();
    }

    private void attachFrontFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                .replace(getMainContainerViewId(), fragment)
                .addToBackStack("place_local_chat")
                .commitAllowingStateLoss();
    }

    @Override
    public void openPlace(Place place) {
        Bundle args = place.getArgs();
        switch (place.getType()) {
            case Place.VK_PHOTO_ALBUM_GALLERY:
            case Place.FAVE_PHOTOS_GALLERY:
            case Place.SIMPLE_PHOTO_GALLERY:
            case Place.VK_PHOTO_TMP_SOURCE:
            case Place.VK_PHOTO_ALBUM_GALLERY_SAVED:
            case Place.VK_PHOTO_ALBUM_GALLERY_NATIVE:
                place.launchActivityForResult(this, PhotoPagerActivity.newInstance(this, place.getType(), args));
                break;
            case Place.SINGLE_PHOTO:
            case Place.GIF_PAGER:
                Intent ph = new Intent(this, PhotoFullScreenActivity.class);
                ph.setAction(PhotoFullScreenActivity.ACTION_OPEN_PLACE);
                ph.putExtra(Extra.PLACE, place);
                startActivity(ph);
                break;

            case Place.DOC_PREVIEW:
                Document document = args.getParcelable(Extra.DOC);
                if (document != null && document.hasValidGifVideoLink()) {
                    int aid = args.getInt(Extra.ACCOUNT_ID);
                    ArrayList<Document> documents = new ArrayList<>(Collections.singletonList(document));
                    Intent gf = new Intent(this, PhotoFullScreenActivity.class);
                    gf.setAction(PhotoFullScreenActivity.ACTION_OPEN_PLACE);
                    gf.putExtra(Extra.PLACE, PlaceFactory.getGifPagerPlace(aid, documents, 0));
                    startActivity(gf);
                } else {
                    Utils.openPlaceWithSwipebleActivity(this, place);
                }
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

    public void onPause() {
        ViewUtils.keyboardHide(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        getSupportFragmentManager().removeOnBackStackChangedListener(mOnBackStackChangedListener);
        ViewUtils.keyboardHide(this);
        super.onDestroy();
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
