package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.domain.IFaveInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IFavePhotosView;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class FavePhotosPresenter extends AccountDependencyPresenter<IFavePhotosView> {

    private static final int COUNT_PER_REQUEST = 50;
    private final IFaveInteractor faveInteractor;
    private final ArrayList<Photo> mPhotos;
    private final CompositeDisposable cacheDisposable = new CompositeDisposable();
    private final CompositeDisposable netDisposable = new CompositeDisposable();
    private boolean mEndOfContent;
    private boolean cacheLoadingNow;
    private boolean requestNow;
    private boolean doLoadTabs;

    public FavePhotosPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        mPhotos = new ArrayList<>();
        faveInteractor = InteractorFactory.createFaveInteractor();

        loadAllCachedData();
    }

    private void resolveRefreshingView() {
        callView(v -> v.showRefreshing(requestNow));
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
        if (doLoadTabs) {
            return;
        } else {
            doLoadTabs = true;
        }
        requestAtLast();
    }

    private void loadAllCachedData() {
        int accountId = getAccountId();

        cacheLoadingNow = true;
        cacheDisposable.add(faveInteractor.getCachedPhotos(accountId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, this::onCacheGetError));
    }

    private void onCacheGetError(Throwable t) {
        cacheLoadingNow = false;
        callView(v -> showError(v, t));
    }

    private void onCachedDataReceived(List<Photo> photos) {
        cacheLoadingNow = false;
        mPhotos.clear();
        mPhotos.addAll(photos);

        callView(IFavePhotosView::notifyDataSetChanged);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        netDisposable.dispose();
        super.onDestroyed();
    }

    private void setRequestNow(boolean requestNow) {
        this.requestNow = requestNow;
        resolveRefreshingView();
    }

    private void request(int offset) {
        setRequestNow(true);

        int accountId = getAccountId();

        netDisposable.add(faveInteractor.getPhotos(accountId, COUNT_PER_REQUEST, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(photos -> onActualDataReceived(offset, photos), this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable t) {
        setRequestNow(false);
        callView(v -> showError(v, getCauseIfRuntime(t)));
    }

    private void onActualDataReceived(int offset, List<Photo> photos) {
        mEndOfContent = photos.isEmpty();
        cacheDisposable.clear();

        setRequestNow(false);

        if (offset == 0) {
            mPhotos.clear();
            mPhotos.addAll(photos);
            callView(IFavePhotosView::notifyDataSetChanged);
        } else {
            int startSize = mPhotos.size();
            mPhotos.addAll(photos);
            callView(view -> view.notifyDataAdded(startSize, photos.size()));
        }
    }

    private void requestAtLast() {
        request(0);
    }

    private void requestNext() {
        request(mPhotos.size());
    }

    @Override
    public void onGuiCreated(@NonNull IFavePhotosView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(mPhotos);
    }

    private boolean canLoadMore() {
        return !mPhotos.isEmpty() && !requestNow && !mEndOfContent && !cacheLoadingNow;
    }

    public void fireRefresh() {
        netDisposable.clear();
        cacheDisposable.clear();
        cacheLoadingNow = false;
        requestNow = false;

        requestAtLast();
    }

    @SuppressWarnings("unused")
    public void firePhotoClick(int position, Photo photo) {
        callView(v -> v.goToGallery(getAccountId(), mPhotos, position));
    }

    public void fireScrollToEnd() {
        if (canLoadMore()) {
            requestNext();
        }
    }
}