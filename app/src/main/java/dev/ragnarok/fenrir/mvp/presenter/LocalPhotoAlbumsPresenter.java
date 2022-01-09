package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.isEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.db.Stores;
import dev.ragnarok.fenrir.model.LocalImageAlbum;
import dev.ragnarok.fenrir.mvp.presenter.base.RxSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.ILocalPhotoAlbumsView;
import dev.ragnarok.fenrir.util.Analytics;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;


public class LocalPhotoAlbumsPresenter extends RxSupportPresenter<ILocalPhotoAlbumsView> {

    private final List<LocalImageAlbum> mLocalImageAlbums;
    private final List<LocalImageAlbum> mLocalImageAlbums_Search;
    private boolean permissionRequestedOnce;
    private boolean mLoadingNow;
    private String q;

    public LocalPhotoAlbumsPresenter(@Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        mLocalImageAlbums = new ArrayList<>();
        mLocalImageAlbums_Search = new ArrayList<>();
    }

    public void fireSearchRequestChanged(String q, boolean force) {
        String query = q == null ? null : q.trim();

        if (!force && Objects.safeEquals(query, this.q)) {
            return;
        }
        this.q = query;
        mLocalImageAlbums_Search.clear();
        if (!isEmpty(this.q)) {
            for (LocalImageAlbum i : mLocalImageAlbums) {
                if (isEmpty(i.getName())) {
                    continue;
                }
                if (i.getName().toLowerCase().contains(this.q.toLowerCase())) {
                    mLocalImageAlbums_Search.add(i);
                }
            }
        }

        if (!isEmpty(this.q))
            callView(v -> v.displayData(mLocalImageAlbums_Search));
        else
            callView(v -> v.displayData(mLocalImageAlbums));
    }

    @Override
    public void onGuiCreated(@NonNull ILocalPhotoAlbumsView viewHost) {
        super.onGuiCreated(viewHost);

        if (!AppPerms.hasReadStoragePermission(getApplicationContext())) {
            if (!permissionRequestedOnce) {
                permissionRequestedOnce = true;
                callView(ILocalPhotoAlbumsView::requestReadExternalStoragePermission);
            }
        } else {
            if (isEmpty(mLocalImageAlbums)) {
                loadData();
                callView(v -> v.displayData(mLocalImageAlbums));
            } else {
                if (isEmpty(q)) {
                    callView(v -> v.displayData(mLocalImageAlbums));
                } else {
                    callView(v -> v.displayData(mLocalImageAlbums_Search));
                }
            }
        }
        resolveProgressView();
        resolveEmptyTextView();
    }

    private void changeLoadingNowState(boolean loading) {
        mLoadingNow = loading;
        resolveProgressView();
    }

    private void resolveProgressView() {
        callView(v -> v.displayProgress(mLoadingNow));
    }

    private void loadData() {
        if (mLoadingNow) return;

        changeLoadingNowState(true);
        appendDisposable(Stores.getInstance()
                .localMedia()
                .getImageAlbums()
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDataLoaded, this::onLoadError));
    }

    private void onLoadError(Throwable throwable) {
        Analytics.logUnexpectedError(throwable);
        changeLoadingNowState(false);
    }

    private void onDataLoaded(List<LocalImageAlbum> data) {
        changeLoadingNowState(false);
        mLocalImageAlbums.clear();
        mLocalImageAlbums.addAll(data);

        callView(ILocalPhotoAlbumsView::notifyDataChanged);

        resolveEmptyTextView();
        if (!isEmpty(q)) {
            fireSearchRequestChanged(q, true);
        }
    }

    private void resolveEmptyTextView() {
        callView(v -> v.setEmptyTextVisible(Utils.safeIsEmpty(mLocalImageAlbums)));
    }

    public void fireRefresh() {
        loadData();
    }

    public void fireAlbumClick(@NonNull LocalImageAlbum album) {
        callView(v -> v.openAlbum(album));
    }

    public void fireReadExternalStoregePermissionResolved() {
        if (AppPerms.hasReadStoragePermission(getApplicationContext())) {
            loadData();
        }
    }
}
