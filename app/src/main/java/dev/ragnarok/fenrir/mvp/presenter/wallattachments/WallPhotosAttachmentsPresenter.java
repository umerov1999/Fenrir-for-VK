package dev.ragnarok.fenrir.mvp.presenter.wallattachments;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.db.Stores;
import dev.ragnarok.fenrir.db.serialize.Serializers;
import dev.ragnarok.fenrir.domain.IWallsRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.TmpSource;
import dev.ragnarok.fenrir.model.criteria.WallCriteria;
import dev.ragnarok.fenrir.module.FenrirNative;
import dev.ragnarok.fenrir.module.parcel.ParcelNative;
import dev.ragnarok.fenrir.mvp.presenter.base.PlaceSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.wallattachments.IWallPhotosAttachmentsView;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Analytics;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class WallPhotosAttachmentsPresenter extends PlaceSupportPresenter<IWallPhotosAttachmentsView> {

    private final ArrayList<Photo> mPhotos;
    private final IWallsRepository fInteractor;
    private final int owner_id;
    private final CompositeDisposable actualDataDisposable = new CompositeDisposable();
    private int loaded;
    private boolean actualDataReceived;
    private boolean endOfContent;
    private boolean actualDataLoading;

    public WallPhotosAttachmentsPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        owner_id = ownerId;
        mPhotos = new ArrayList<>();
        fInteractor = Repository.INSTANCE.getWalls();
        loadActualData(0);
    }

    @Override
    public void onGuiCreated(@NonNull IWallPhotosAttachmentsView view) {
        super.onGuiCreated(view);
        view.displayData(mPhotos);

        resolveToolbar();
    }

    @SuppressWarnings("unused")
    public void firePhotoClick(int position, Photo photo) {
        if (FenrirNative.isNativeLoaded() && Settings.get().other().isNative_parcel_photo()) {
            callView(view -> view.goToTempPhotosGallery(getAccountId(), ParcelNative.create().writeParcelableList(mPhotos).getNativePointer(), position));
        } else {
            TmpSource source = new TmpSource(getInstanceId(), 0);

            fireTempDataUsage();

            actualDataDisposable.add(Stores.getInstance()
                    .tempStore()
                    .put(source.getOwnerId(), source.getSourceId(), mPhotos, Serializers.PHOTOS_SERIALIZER)
                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                    .subscribe(() -> onPhotosSavedToTmpStore(position, source), Analytics::logUnexpectedError));
        }
    }

    private void onPhotosSavedToTmpStore(int index, TmpSource source) {
        callView(view -> view.goToTempPhotosGallery(getAccountId(), source, index));
    }

    private void loadActualData(int offset) {
        actualDataLoading = true;

        resolveRefreshingView();

        int accountId = getAccountId();
        actualDataDisposable.add(fInteractor.getWallNoCache(accountId, owner_id, offset, 100, WallCriteria.MODE_ALL)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onActualDataReceived(offset, data), this::onActualDataGetError));

    }

    private void onActualDataGetError(Throwable t) {
        actualDataLoading = false;
        callView(v -> showError(v, getCauseIfRuntime(t)));

        resolveRefreshingView();
    }

    private void update(List<Post> data) {
        for (Post i : data) {
            if (i.hasAttachments() && !isEmpty(i.getAttachments().getPhotos()))
                mPhotos.addAll(i.getAttachments().getPhotos());
            if (i.hasCopyHierarchy())
                update(i.getCopyHierarchy());
        }
    }

    private void onActualDataReceived(int offset, List<Post> data) {

        actualDataLoading = false;
        endOfContent = data.isEmpty();
        actualDataReceived = true;
        if (endOfContent)
            callResumedView(v -> v.onSetLoadingStatus(2));

        if (offset == 0) {
            loaded = data.size();
            mPhotos.clear();
            update(data);
            resolveToolbar();
            callView(IWallPhotosAttachmentsView::notifyDataSetChanged);
        } else {
            int startSize = mPhotos.size();
            loaded += data.size();
            update(data);
            resolveToolbar();
            callView(view -> view.notifyDataAdded(startSize, mPhotos.size() - startSize));
        }

        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        callResumedView(v -> v.showRefreshing(actualDataLoading));
        if (!endOfContent)
            callResumedView(v -> v.onSetLoadingStatus(actualDataLoading ? 1 : 0));
    }

    private void resolveToolbar() {
        callView(v -> {
            v.setToolbarTitle(getString(R.string.attachments_in_wall));
            v.setToolbarSubtitle(getString(R.string.photos_count, safeCountOf(mPhotos)) + " " + getString(R.string.posts_analized, loaded));
        });
    }

    @Override
    public void onDestroyed() {
        actualDataDisposable.dispose();
        super.onDestroyed();
    }

    public boolean fireScrollToEnd() {
        if (!endOfContent && actualDataReceived && !actualDataLoading) {
            loadActualData(loaded);
            return false;
        }
        return true;
    }

    public void fireRefresh() {

        actualDataDisposable.clear();
        actualDataLoading = false;

        loadActualData(0);
    }
}
