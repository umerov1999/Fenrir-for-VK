package dev.ragnarok.fenrir.activity;

import static dev.ragnarok.fenrir.util.Objects.isNull;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.fragment.DualTabPhotosFragment;
import dev.ragnarok.fenrir.fragment.LocalPhotosFragment;
import dev.ragnarok.fenrir.fragment.SinglePhotoFragment;
import dev.ragnarok.fenrir.fragment.VKPhotosFragment;
import dev.ragnarok.fenrir.model.LocalImageAlbum;
import dev.ragnarok.fenrir.model.selection.Sources;
import dev.ragnarok.fenrir.mvp.view.IVkPhotosView;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceProvider;

public class DualTabPhotoActivity extends NoMainActivity implements PlaceProvider {

    private int mMaxSelectionCount;
    private Sources mSources;

    public static Intent createIntent(Context context, int maxSelectionCount, @NonNull Sources sources) {
        return new Intent(context, DualTabPhotoActivity.class)
                .putExtra(Extra.MAX_COUNT, maxSelectionCount)
                .putExtra(Extra.SOURCES, sources);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isNull(savedInstanceState)) {
            mMaxSelectionCount = getIntent().getIntExtra(Extra.MAX_COUNT, 10);
            mSources = getIntent().getParcelableExtra(Extra.SOURCES);

            attachStartFragment();
        } else {
            mMaxSelectionCount = savedInstanceState.getInt("mMaxSelectionCount");
            mSources = savedInstanceState.getParcelable("mSources");
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mMaxSelectionCount", mMaxSelectionCount);
        outState.putParcelable("mSources", mSources);
    }

    private void attachStartFragment() {
        DualTabPhotosFragment fragment = DualTabPhotosFragment.newInstance(mSources);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                .replace(getMainContainerViewId(), fragment)
                .addToBackStack("dual-tab-photos")
                .commit();
    }

    @Override
    public void openPlace(Place place) {
        switch (place.getType()) {
            case Place.VK_PHOTO_ALBUM:
                int albumId = place.getArgs().getInt(Extra.ALBUM_ID);
                int accountId = place.getArgs().getInt(Extra.ACCOUNT_ID);
                int ownerId = place.getArgs().getInt(Extra.OWNER_ID);

                VKPhotosFragment fragment = VKPhotosFragment.newInstance(accountId, ownerId, albumId, IVkPhotosView.ACTION_SELECT_PHOTOS);

                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                        .replace(R.id.fragment, fragment)
                        .addToBackStack("vk-album-photos")
                        .commit();
                break;

            case Place.VK_INTERNAL_PLAYER:
                Intent intent = new Intent(this, VideoPlayerActivity.class);
                intent.putExtras(place.getArgs());
                startActivity(intent);
                break;

            case Place.SINGLE_PHOTO:
                SinglePhotoFragment previewPhotosFragment = SinglePhotoFragment.newInstance(place.getArgs());
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                        .replace(R.id.fragment, previewPhotosFragment)
                        .addToBackStack("preview")
                        .commit();
                break;

            case Place.LOCAL_IMAGE_ALBUM:
                LocalImageAlbum album = place.getArgs().getParcelable(Extra.ALBUM);

                LocalPhotosFragment localPhotosFragment = LocalPhotosFragment.newInstance(mMaxSelectionCount, album, false);
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fragment_enter_pop, R.anim.fragment_exit_pop)
                        .replace(R.id.fragment, localPhotosFragment)
                        .addToBackStack("local-album-photos")
                        .commit();
                break;
        }
    }
}
