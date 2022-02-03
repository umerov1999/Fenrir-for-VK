package dev.ragnarok.fenrir.mvp.presenter.photo;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.db.Stores;
import dev.ragnarok.fenrir.db.serialize.Serializers;
import dev.ragnarok.fenrir.domain.ILocalServerInteractor;
import dev.ragnarok.fenrir.domain.IPhotosInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.TmpSource;
import dev.ragnarok.fenrir.module.FenrirNative;
import dev.ragnarok.fenrir.module.parcel.ParcelFlags;
import dev.ragnarok.fenrir.module.parcel.ParcelNative;
import dev.ragnarok.fenrir.mvp.view.IPhotoPagerView;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Analytics;
import dev.ragnarok.fenrir.util.RxUtils;

public class PhotoAlbumPagerPresenter extends PhotoPagerPresenter {

    private static final int COUNT_PER_LOAD = 100;
    private final IPhotosInteractor photosInteractor;
    private final ILocalServerInteractor localServerInteractor;
    private final int mOwnerId;
    private final int mAlbumId;
    private final boolean invertPhotoRev;
    private boolean canLoad;

    public PhotoAlbumPagerPresenter(int index, int accountId, int ownerId, int albumId, ArrayList<Photo> photos, boolean readOnly, boolean invertPhotoRev, Context context,
                                    @Nullable Bundle savedInstanceState) {
        super(new ArrayList<>(0), accountId, readOnly, context, savedInstanceState);
        photosInteractor = InteractorFactory.createPhotosInteractor();
        localServerInteractor = InteractorFactory.createLocalServerInteractor();
        mOwnerId = ownerId;
        mAlbumId = albumId;
        canLoad = true;
        this.invertPhotoRev = invertPhotoRev;

        getData().addAll(photos);
        setCurrentIndex(index);

        refreshPagerView();
        resolveButtonsBarVisible();
        resolveToolbarVisibility();
        refreshInfoViews(true);
    }

    public PhotoAlbumPagerPresenter(int index, int accountId, int ownerId, int albumId, @NonNull TmpSource source, boolean readOnly, boolean invertPhotoRev, Context context,
                                    @Nullable Bundle savedInstanceState) {
        super(new ArrayList<>(0), accountId, readOnly, context, savedInstanceState);
        photosInteractor = InteractorFactory.createPhotosInteractor();
        localServerInteractor = InteractorFactory.createLocalServerInteractor();
        mOwnerId = ownerId;
        mAlbumId = albumId;
        canLoad = true;
        this.invertPhotoRev = invertPhotoRev;

        setCurrentIndex(index);
        loadDataFromDatabase(source);
    }

    private void loadDataFromDatabase(TmpSource source) {
        changeLoadingNowState(true);
        appendDisposable(Stores.getInstance()
                .tempStore()
                .getData(source.getOwnerId(), source.getSourceId(), Serializers.PHOTOS_SERIALIZER)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onInitialLoadingFinished, Analytics::logUnexpectedError));
    }

    private void onInitialLoadingFinished(List<Photo> photos) {
        changeLoadingNowState(false);

        getData().addAll(photos);

        refreshPagerView();
        resolveButtonsBarVisible();
        resolveToolbarVisibility();
        refreshInfoViews(true);
    }

    @Override
    protected boolean need_update_info() {
        return true;
    }

    @Override
    void initPhotosData(@NonNull ArrayList<Photo> initialData, @Nullable Bundle savedInstanceState) {
        mPhotos = initialData;
    }

    @Override
    void savePhotosState(@NonNull Bundle outState) {
        //no saving state
    }

    private void loadData() {
        if (!canLoad)
            return;
        changeLoadingNowState(true);

        if (mAlbumId != -9001 && mAlbumId != -9000 && mAlbumId != -311) {
            appendDisposable(photosInteractor.get(getAccountId(), mOwnerId, mAlbumId, COUNT_PER_LOAD, mPhotos.size(), !invertPhotoRev)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onActualPhotosReceived, this::onActualDataGetError));
        } else if (mAlbumId == -9000) {
            appendDisposable(photosInteractor.getUsersPhoto(getAccountId(), mOwnerId, 1, invertPhotoRev ? 1 : 0, mPhotos.size(), COUNT_PER_LOAD)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onActualPhotosReceived, this::onActualDataGetError));
        } else if (mAlbumId == -9001) {
            appendDisposable(photosInteractor.getAll(getAccountId(), mOwnerId, 1, 1, mPhotos.size(), COUNT_PER_LOAD)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onActualPhotosReceived, this::onActualDataGetError));
        } else {
            appendDisposable(localServerInteractor.getPhotos(mPhotos.size(), COUNT_PER_LOAD, invertPhotoRev)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onActualPhotosReceived, this::onActualDataGetError));
        }
    }

    private void onActualDataGetError(Throwable t) {
        callView(v -> showError(v, getCauseIfRuntime(t)));
    }

    @Override
    public void close() {
        if (Settings.get().other().isNative_parcel_photo() && FenrirNative.isNativeLoaded()) {
            long ptr = ParcelNative.createParcelableList(getData(), ParcelFlags.NULL_LIST);
            callView(v -> v.returnInfo(getCurrentIndex(), ptr));
        } else {
            callView(IPhotoPagerView::closeOnly);
        }
    }

    private void onActualPhotosReceived(List<Photo> data) {
        changeLoadingNowState(false);
        if (data.isEmpty()) {
            canLoad = false;
            return;
        }

        getData().addAll(data);

        refreshPagerView();
        resolveButtonsBarVisible();
        resolveToolbarVisibility();
        refreshInfoViews(true);
    }

    @Override
    protected void afterPageChangedFromUi(int oldPage, int newPage) {
        super.afterPageChangedFromUi(oldPage, newPage);
        if (oldPage == newPage)
            return;

        if (newPage == count() - 1) {
            loadData();
        }
    }
}