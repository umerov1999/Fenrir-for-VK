package dev.ragnarok.fenrir.mvp.presenter.photo;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.db.Stores;
import dev.ragnarok.fenrir.db.serialize.Serializers;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.TmpSource;
import dev.ragnarok.fenrir.module.parcel.ParcelNative;
import dev.ragnarok.fenrir.util.Analytics;
import dev.ragnarok.fenrir.util.RxUtils;

public class TmpGalleryPagerPresenter extends PhotoPagerPresenter {

    public TmpGalleryPagerPresenter(int accountId, @NonNull TmpSource source, int index, Context context,
                                    @Nullable Bundle savedInstanceState) {
        super(new ArrayList<>(0), accountId, false, context, savedInstanceState);
        setCurrentIndex(index);
        loadDataFromDatabase(source);
    }

    public TmpGalleryPagerPresenter(int accountId, long source, int index, Context context,
                                    @Nullable Bundle savedInstanceState) {
        super(new ArrayList<>(0), accountId, false, context, savedInstanceState);
        setCurrentIndex(index);
        changeLoadingNowState(true);
        onInitialLoadingFinished(ParcelNative.fromNative(source).readParcelableList(Photo.NativeCreator));
    }

    @Override
    public void close() {
        callView(v -> v.returnOnlyPos(getCurrentIndex()));
    }

    @Override
    void initPhotosData(@NonNull ArrayList<Photo> initialData, @Nullable Bundle savedInstanceState) {
        mPhotos = initialData;
    }

    @Override
    void savePhotosState(@NonNull Bundle outState) {
        // no saving
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
}
