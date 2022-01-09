package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.domain.IFaveInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IFaveVideosView;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class FaveVideosPresenter extends AccountDependencyPresenter<IFaveVideosView> {

    private static final int COUNT_PER_REQUEST = 25;
    private final IFaveInteractor faveInteractor;
    private final ArrayList<Video> mVideos;
    private final CompositeDisposable cacheDisposable = new CompositeDisposable();
    private final CompositeDisposable netDisposable = new CompositeDisposable();
    private boolean mEndOfContent;
    private boolean cacheLoadingNow;
    private boolean netLoadingNow;
    private boolean doLoadTabs;

    public FaveVideosPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        faveInteractor = InteractorFactory.createFaveInteractor();
        mVideos = new ArrayList<>();

        loadCachedData();
    }

    private void resolveRefreshingView() {
        callView(v -> v.showRefreshing(netLoadingNow));
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

    private void loadCachedData() {
        cacheLoadingNow = true;

        int accountId = getAccountId();
        cacheDisposable.add(faveInteractor.getCachedVideos(accountId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, this::onCacheGetError));
    }

    private void onCacheGetError(Throwable t) {
        cacheLoadingNow = false;
        callView(v -> showError(v, t));
    }

    private void onCachedDataReceived(List<Video> videos) {
        cacheLoadingNow = false;

        mVideos.clear();
        mVideos.addAll(videos);
        callView(IFaveVideosView::notifyDataSetChanged);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        netDisposable.dispose();
        super.onDestroyed();
    }

    private void request(int offset) {
        netLoadingNow = true;
        resolveRefreshingView();

        int accountId = getAccountId();

        netDisposable.add(faveInteractor.getVideos(accountId, COUNT_PER_REQUEST, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(videos -> onNetDataReceived(offset, videos), this::onNetDataGetError));
    }

    private void onNetDataGetError(Throwable t) {
        netLoadingNow = false;
        resolveRefreshingView();
        callView(v -> showError(v, t));
    }

    private void onNetDataReceived(int offset, List<Video> videos) {
        cacheDisposable.clear();
        cacheLoadingNow = false;

        mEndOfContent = videos.isEmpty();
        netLoadingNow = false;

        if (offset == 0) {
            mVideos.clear();
            mVideos.addAll(videos);
            callView(IFaveVideosView::notifyDataSetChanged);
        } else {
            int startSize = mVideos.size();
            mVideos.addAll(videos);
            callView(view -> view.notifyDataAdded(startSize, videos.size()));
        }

        resolveRefreshingView();
    }

    private void requestAtLast() {
        request(0);
    }

    private void requestNext() {
        request(mVideos.size());
    }

    @Override
    public void onGuiCreated(@NonNull IFaveVideosView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(mVideos);
    }

    private boolean canLoadMore() {
        return !mVideos.isEmpty() && !cacheLoadingNow && !netLoadingNow && !mEndOfContent;
    }

    public void fireRefresh() {
        cacheDisposable.clear();
        netDisposable.clear();
        netLoadingNow = false;

        requestAtLast();
    }

    public void fireVideoClick(Video video) {
        callView(v -> v.goToPreview(getAccountId(), video));
    }

    public void fireVideoDelete(int index, Video video) {
        netDisposable.add(faveInteractor.removeVideo(getAccountId(), video.getOwnerId(), video.getId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(videos -> {
                    mVideos.remove(index);
                    callView(IFaveVideosView::notifyDataSetChanged);
                }, this::onNetDataGetError));
    }

    public void fireScrollToEnd() {
        if (canLoadMore()) {
            requestNext();
        }
    }
}