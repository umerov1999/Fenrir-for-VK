package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.db.Stores;
import dev.ragnarok.fenrir.model.LocalImageAlbum;
import dev.ragnarok.fenrir.model.LocalPhoto;
import dev.ragnarok.fenrir.mvp.presenter.base.RxSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.ILocalPhotosView;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;

public class LocalPhotosPresenter extends RxSupportPresenter<ILocalPhotosView> {

    private final LocalImageAlbum mLocalImageAlbum;
    private final int mSelectionCountMax;

    private List<LocalPhoto> mLocalPhotos;
    private boolean mLoadingNow;

    public LocalPhotosPresenter(LocalImageAlbum album, int maxSelectionCount,
                                @Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        mLocalImageAlbum = album;
        mSelectionCountMax = maxSelectionCount;

        mLocalPhotos = Collections.emptyList();
        /*
        if(mLocalImageAlbum == null && !AppPerms.hasReadStoragePermission(getApplicationContext())){
            if(!permissionRequestedOnce){
                permissionRequestedOnce = true;
                callView(v -> v.requestReadExternalStoragePermission();
            }
        } else {
            loadData();
        }
         */
        loadData();
    }

    private void loadData() {
        if (mLoadingNow) return;

        changeLoadingState(true);
        if (mLocalImageAlbum != null) {
            appendDisposable(Stores.getInstance()
                    .localMedia()
                    .getPhotos(mLocalImageAlbum.getId())
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onDataLoaded, this::onLoadError));
        } else {
            appendDisposable(Stores.getInstance()
                    .localMedia()
                    .getPhotos()
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onDataLoaded, this::onLoadError));
        }
    }

    private void onLoadError(Throwable throwable) {
        changeLoadingState(false);
    }

    private void onDataLoaded(List<LocalPhoto> data) {
        changeLoadingState(false);
        mLocalPhotos = data;
        resolveListData();
        resolveEmptyTextVisibility();
    }

    @Override
    public void onGuiCreated(@NonNull ILocalPhotosView viewHost) {
        super.onGuiCreated(viewHost);
        resolveListData();
        resolveProgressView();
        resolveFabVisibility(false);
        resolveEmptyTextVisibility();
    }

    private void resolveEmptyTextVisibility() {
        callView(v -> v.setEmptyTextVisible(Utils.safeIsEmpty(mLocalPhotos)));
    }

    private void resolveListData() {
        callView(v -> v.displayData(mLocalPhotos));
    }

    private void changeLoadingState(boolean loading) {
        mLoadingNow = loading;
        resolveProgressView();
    }

    private void resolveProgressView() {
        callView(v -> v.displayProgress(mLoadingNow));
    }

    public void fireFabClick() {
        ArrayList<LocalPhoto> localPhotos = Utils.getSelected(mLocalPhotos);
        if (!localPhotos.isEmpty()) {
            callView(v -> v.returnResultToParent(localPhotos));
        } else {
            callView(v -> v.showError(R.string.select_attachments));
        }
    }


    public void firePhotoClick(@NonNull LocalPhoto photo) {
        photo.setSelected(!photo.isSelected());

        if (mSelectionCountMax == 1 && photo.isSelected()) {
            ArrayList<LocalPhoto> single = new ArrayList<>(1);
            single.add(photo);
            callView(v -> v.returnResultToParent(single));
            return;
        }

        onSelectPhoto(photo);
        callView(ILocalPhotosView::updateSelectionAndIndexes);
    }

    private void onSelectPhoto(LocalPhoto selectedPhoto) {
        if (selectedPhoto.isSelected()) {
            int targetIndex = 1;
            for (LocalPhoto photo : mLocalPhotos) {
                if (photo.getIndex() >= targetIndex) {
                    targetIndex = photo.getIndex() + 1;
                }
            }

            selectedPhoto.setIndex(targetIndex);
        } else {
            for (int i = 0; i < mLocalPhotos.size(); i++) {
                LocalPhoto photo = mLocalPhotos.get(i);
                if (photo.getIndex() > selectedPhoto.getIndex()) {
                    photo.setIndex(photo.getIndex() - 1);
                }
            }

            selectedPhoto.setIndex(0);
        }

        if (selectedPhoto.isSelected()) {
            resolveFabVisibility(true, true);
        } else {
            resolveFabVisibility(true);
        }
    }

    private void resolveFabVisibility(boolean anim) {
        resolveFabVisibility(Utils.countOfSelection(mLocalPhotos) > 0, anim);
    }

    private void resolveFabVisibility(boolean visible, boolean anim) {
        callView(v -> v.setFabVisible(visible, anim));
    }

    public void fireRefresh() {
        loadData();
    }

    public void fireReadExternalStoregePermissionResolved() {
        if (AppPerms.hasReadStoragePermission(getApplicationContext())) {
            loadData();
        }
    }
}
