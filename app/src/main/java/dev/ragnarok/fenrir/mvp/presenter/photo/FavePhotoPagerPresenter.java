package dev.ragnarok.fenrir.mvp.presenter.photo;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.model.AccessIdPair;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.util.RxUtils;

public class FavePhotoPagerPresenter extends PhotoPagerPresenter {

    private static final String SAVE_UPDATED = "save_updated";

    private final boolean[] mUpdated;
    private final boolean[] refreshing;

    public FavePhotoPagerPresenter(@NonNull ArrayList<Photo> photos, int index, int accountId, Context context, @Nullable Bundle savedInstanceState) {
        super(photos, accountId, false, context, savedInstanceState);
        refreshing = new boolean[photos.size()];

        if (savedInstanceState == null) {
            mUpdated = new boolean[photos.size()];
            setCurrentIndex(index);
            refresh(index);
        } else {
            mUpdated = savedInstanceState.getBooleanArray(SAVE_UPDATED);
        }
    }

    @Override
    public void close() {
        callView(v -> v.returnOnlyPos(getCurrentIndex()));
    }

    private void refresh(int index) {
        if (mUpdated[index] || refreshing[index]) {
            return;
        }

        refreshing[index] = true;

        Photo photo = getData().get(index);
        int accountId = getAccountId();

        List<AccessIdPair> forUpdate = Collections.singletonList(new AccessIdPair(photo.getId(), photo.getOwnerId(), photo.getAccessKey()));
        appendDisposable(photosInteractor.getPhotosByIds(accountId, forUpdate)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(photos -> onPhotoUpdateReceived(photos, index), t -> onRefreshFailed(index, t)));
    }

    private void onRefreshFailed(int index, Throwable t) {
        refreshing[index] = false;
        callView(v -> showError(v, getCauseIfRuntime(t)));
    }

    private void onPhotoUpdateReceived(List<Photo> result, int index) {
        refreshing[index] = false;

        if (result.size() == 1) {
            Photo p = result.get(0);

            getData().set(index, p);

            mUpdated[index] = true;

            if (getCurrentIndex() == index) {
                refreshInfoViews(true);
            }
        }
    }

    @Override
    protected void afterPageChangedFromUi(int oldPage, int newPage) {
        super.afterPageChangedFromUi(oldPage, newPage);
        refresh(newPage);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putBooleanArray(SAVE_UPDATED, mUpdated);
    }
}