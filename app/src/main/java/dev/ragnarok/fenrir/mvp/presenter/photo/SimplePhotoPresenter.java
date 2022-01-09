package dev.ragnarok.fenrir.mvp.presenter.photo;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.model.AccessIdPair;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.util.RxUtils;

public class SimplePhotoPresenter extends PhotoPagerPresenter {

    private static final String SAVE_DATA_REFRESH_RESULT = "save-data-refresh-result";
    private boolean mDataRefreshSuccessfull;

    public SimplePhotoPresenter(@NonNull ArrayList<Photo> photos, int index, boolean needToRefreshData,
                                int accountId, Context context, @Nullable Bundle savedInstanceState) {
        super(photos, accountId, !needToRefreshData, context, savedInstanceState);

        if (savedInstanceState == null) {
            setCurrentIndex(index);
        } else {
            mDataRefreshSuccessfull = savedInstanceState.getBoolean(SAVE_DATA_REFRESH_RESULT);
        }

        if (needToRefreshData && !mDataRefreshSuccessfull) {
            refreshData();
        }
    }

    private void refreshData() {
        ArrayList<AccessIdPair> ids = new ArrayList<>(getData().size());
        int accountId = getAccountId();

        for (Photo photo : getData()) {
            ids.add(new AccessIdPair(photo.getId(), photo.getOwnerId(), photo.getAccessKey()));
        }

        appendDisposable(photosInteractor.getPhotosByIds(accountId, ids)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPhotosReceived, t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
    }

    private void onPhotosReceived(List<Photo> photos) {
        mDataRefreshSuccessfull = true;
        onPhotoListRefresh(photos);
    }

    private void onPhotoListRefresh(@NonNull List<Photo> photos) {
        List<Photo> originalData = getData();

        for (Photo photo : photos) {
            //замена старых обьектов новыми
            for (int i = 0; i < originalData.size(); i++) {
                Photo orig = originalData.get(i);

                if (orig.getId() == photo.getId() && orig.getOwnerId() == photo.getOwnerId()) {
                    originalData.set(i, photo);

                    // если у фото до этого не было ссылок на файлы
                    if (isNull(orig.getSizes()) || orig.getSizes().isEmpty()) {
                        int finalI = i;
                        callView(v -> v.rebindPhotoAt(finalI));
                    }
                    break;
                }
            }
        }

        refreshInfoViews(true);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putBoolean(SAVE_DATA_REFRESH_RESULT, mDataRefreshSuccessfull);
    }
}