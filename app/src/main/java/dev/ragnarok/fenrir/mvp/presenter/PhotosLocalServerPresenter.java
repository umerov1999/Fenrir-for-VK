package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import dev.ragnarok.fenrir.db.Stores;
import dev.ragnarok.fenrir.db.serialize.Serializers;
import dev.ragnarok.fenrir.domain.ILocalServerInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.TmpSource;
import dev.ragnarok.fenrir.module.FenrirNative;
import dev.ragnarok.fenrir.module.parcel.ParcelNative;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IPhotosLocalServerView;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Analytics;
import dev.ragnarok.fenrir.util.FindAt;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;

public class PhotosLocalServerPresenter extends AccountDependencyPresenter<IPhotosLocalServerView> {

    private static final int SEARCH_COUNT = 50;
    private static final int GET_COUNT = 100;
    private static final int WEB_SEARCH_DELAY = 1000;
    private final List<Photo> photos;
    private final ILocalServerInteractor fInteractor;
    private Disposable actualDataDisposable = Disposable.disposed();
    private int Foffset;
    private boolean actualDataReceived;
    private boolean endOfContent;
    private boolean actualDataLoading;
    private boolean reverse;
    private FindAt search_at;
    private boolean doLoadTabs;

    public PhotosLocalServerPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        photos = new ArrayList<>();
        fInteractor = InteractorFactory.createLocalServerInteractor();
        search_at = new FindAt();
    }

    public void toggleReverse() {
        reverse = !reverse;
        fireRefresh(false);
    }

    @Override
    public void onGuiCreated(@NonNull IPhotosLocalServerView view) {
        super.onGuiCreated(view);
        view.displayList(photos);
    }

    private void loadActualData(int offset) {
        actualDataLoading = true;

        resolveRefreshingView();

        appendDisposable(fInteractor.getPhotos(offset, GET_COUNT, reverse)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onActualDataReceived(offset, data), this::onActualDataGetError));

    }

    private void onActualDataGetError(Throwable t) {
        actualDataLoading = false;
        callView(v -> showError(v, getCauseIfRuntime(t)));

        resolveRefreshingView();
    }

    private void onActualDataReceived(int offset, List<Photo> data) {
        Foffset = offset + GET_COUNT;
        actualDataLoading = false;
        endOfContent = data.isEmpty();
        actualDataReceived = true;

        if (offset == 0) {
            photos.clear();
            photos.addAll(data);
            callView(IPhotosLocalServerView::notifyListChanged);
        } else {
            int startSize = photos.size();
            photos.addAll(data);
            callView(view -> view.notifyDataAdded(startSize, data.size()));
        }

        resolveRefreshingView();
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
        loadActualData(0);
    }

    private void resolveRefreshingView() {
        callResumedView(v -> v.displayLoading(actualDataLoading));
    }

    @Override
    public void onDestroyed() {
        actualDataDisposable.dispose();
        super.onDestroyed();
    }

    public boolean fireScrollToEnd() {
        if (!endOfContent && nonEmpty(photos) && actualDataReceived && !actualDataLoading) {
            if (search_at.isSearchMode()) {
                search(false);
            } else {
                loadActualData(Foffset);
            }
            return false;
        }
        return true;
    }

    private void doSearch() {
        actualDataLoading = true;
        resolveRefreshingView();
        appendDisposable(fInteractor.searchPhotos(search_at.getQuery(), search_at.getOffset(), SEARCH_COUNT, reverse)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onSearched(new FindAt(Objects.requireNonNull(search_at.getQuery()), search_at.getOffset() + SEARCH_COUNT, data.size() < SEARCH_COUNT), data), this::onActualDataGetError));
    }

    private void onSearched(FindAt search_at, List<Photo> data) {
        actualDataLoading = false;
        actualDataReceived = true;
        endOfContent = search_at.isEnded();

        if (this.search_at.getOffset() == 0) {
            photos.clear();
            photos.addAll(data);
            callView(IPhotosLocalServerView::notifyListChanged);
        } else {
            if (nonEmpty(data)) {
                int startSize = photos.size();
                photos.addAll(data);
                callView(view -> view.notifyDataAdded(startSize, data.size()));
            }
        }
        this.search_at = search_at;
        resolveRefreshingView();
    }

    private void search(boolean sleep_search) {
        if (actualDataLoading) return;

        if (!sleep_search) {
            doSearch();
            return;
        }

        actualDataDisposable.dispose();
        actualDataDisposable = (Single.just(new Object())
                .delay(WEB_SEARCH_DELAY, TimeUnit.MILLISECONDS)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(videos -> doSearch(), this::onActualDataGetError));
    }

    public void fireSearchRequestChanged(String q) {
        String query = q == null ? null : q.trim();
        if (!search_at.do_compare(query)) {
            actualDataLoading = false;
            if (Utils.isEmpty(query)) {
                actualDataDisposable.dispose();
                fireRefresh(false);
            } else {
                fireRefresh(true);
            }
        }
    }

    public void updateInfo(int position, long ptr) {
        List<Photo> p = ParcelNative.fromNative(ptr).readParcelableList(Photo.NativeCreator);
        photos.clear();
        photos.addAll(p);
        callView(v -> v.scrollTo(position));
    }

    public void firePhotoClick(Photo wrapper) {
        int Index = 0;
        boolean trig = false;
        if (!FenrirNative.isNativeLoaded() || !Settings.get().other().isNative_parcel_photo()) {
            for (int i = 0; i < photos.size(); i++) {
                Photo photo = photos.get(i);
                if (!trig && photo.getId() == wrapper.getId() && photo.getOwnerId() == wrapper.getOwnerId()) {
                    Index = i;
                    trig = true;
                }
            }
            int finalIndex = Index;
            TmpSource source = new TmpSource(getInstanceId(), 0);
            fireTempDataUsage();
            appendDisposable(Stores.getInstance()
                    .tempStore()
                    .put(source.getOwnerId(), source.getSourceId(), photos, Serializers.PHOTOS_SERIALIZER)
                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                    .subscribe(() -> callView(view -> view.displayGallery(getAccountId(), -311, getAccountId(), source, finalIndex, reverse)), Analytics::logUnexpectedError));
        } else {
            ParcelNative mem = ParcelNative.create();
            mem.writeInt(photos.size());
            for (int i = 0; i < photos.size(); i++) {
                Photo photo = photos.get(i);
                mem.writeParcelable(photo);
                if (!trig && photo.getId() == wrapper.getId() && photo.getOwnerId() == wrapper.getOwnerId()) {
                    Index = i;
                    trig = true;
                }
            }
            int finalIndex = Index;
            callView(view -> view.displayGalleryUnSafe(getAccountId(), -311, getAccountId(), mem.getNativePointer(), finalIndex, reverse));
        }
    }

    public void fireRefresh(boolean sleep_search) {
        if (actualDataLoading) {
            return;
        }

        if (search_at.isSearchMode()) {
            search_at.reset(false);
            search(sleep_search);
        } else {
            loadActualData(0);
        }
    }
}
