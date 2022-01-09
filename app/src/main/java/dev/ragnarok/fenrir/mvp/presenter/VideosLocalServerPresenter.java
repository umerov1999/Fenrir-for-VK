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

import dev.ragnarok.fenrir.domain.ILocalServerInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IVideosLocalServerView;
import dev.ragnarok.fenrir.util.FindAt;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;

public class VideosLocalServerPresenter extends AccountDependencyPresenter<IVideosLocalServerView> {

    private static final int SEARCH_COUNT = 50;
    private static final int GET_COUNT = 100;
    private static final int WEB_SEARCH_DELAY = 1000;
    private final List<Video> videos;
    private final ILocalServerInteractor fInteractor;
    private Disposable actualDataDisposable = Disposable.disposed();
    private int Foffset;
    private boolean actualDataReceived;
    private boolean endOfContent;
    private boolean actualDataLoading;
    private FindAt search_at;
    private boolean reverse;
    private boolean doLoadTabs;

    public VideosLocalServerPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        videos = new ArrayList<>();
        fInteractor = InteractorFactory.createLocalServerInteractor();
        search_at = new FindAt();
    }

    @Override
    public void onGuiCreated(@NonNull IVideosLocalServerView view) {
        super.onGuiCreated(view);
        view.displayList(videos);
    }

    public void toggleReverse() {
        reverse = !reverse;
        fireRefresh(false);
    }

    private void loadActualData(int offset) {
        actualDataLoading = true;

        resolveRefreshingView();

        appendDisposable(fInteractor.getVideos(offset, GET_COUNT, reverse)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onActualDataReceived(offset, data), this::onActualDataGetError));

    }

    private void onActualDataGetError(Throwable t) {
        actualDataLoading = false;
        callView(v -> showError(v, getCauseIfRuntime(t)));

        resolveRefreshingView();
    }

    private void onActualDataReceived(int offset, List<Video> data) {
        Foffset = offset + GET_COUNT;
        actualDataLoading = false;
        endOfContent = data.isEmpty();
        actualDataReceived = true;

        if (offset == 0) {
            videos.clear();
            videos.addAll(data);
            callView(IVideosLocalServerView::notifyListChanged);
        } else {
            int startSize = videos.size();
            videos.addAll(data);
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
        if (!endOfContent && nonEmpty(videos) && actualDataReceived && !actualDataLoading) {
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
        appendDisposable(fInteractor.searchVideos(search_at.getQuery(), search_at.getOffset(), SEARCH_COUNT, reverse)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onSearched(new FindAt(Objects.requireNonNull(search_at.getQuery()), search_at.getOffset() + SEARCH_COUNT, data.size() < SEARCH_COUNT), data), this::onActualDataGetError));
    }

    private void onSearched(FindAt search_at, List<Video> data) {
        actualDataLoading = false;
        actualDataReceived = true;
        endOfContent = search_at.isEnded();

        if (this.search_at.getOffset() == 0) {
            videos.clear();
            videos.addAll(data);
            callView(IVideosLocalServerView::notifyListChanged);
        } else {
            if (nonEmpty(data)) {
                int startSize = videos.size();
                videos.addAll(data);
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
