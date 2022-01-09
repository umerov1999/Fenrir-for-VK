package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.findIndexById;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dev.ragnarok.fenrir.domain.IFaveInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.FavePage;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IFaveUsersView;
import dev.ragnarok.fenrir.util.FindAtWithContent;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;


public class FavePagesPresenter extends AccountDependencyPresenter<IFaveUsersView> {
    private static final int SEARCH_COUNT = 250;
    private static final int SEARCH_VIEW_COUNT = 20;
    private static final int GET_COUNT = 500;
    private static final int WEB_SEARCH_DELAY = 1000;
    private final List<FavePage> pages;
    private final IFaveInteractor faveInteractor;
    private final boolean isUser;
    private final CompositeDisposable cacheDisposable = new CompositeDisposable();
    private final CompositeDisposable actualDataDisposable = new CompositeDisposable();
    private final FindPage searcher;
    private Disposable sleepDataDisposable = Disposable.disposed();
    private boolean actualDataReceived;
    private boolean endOfContent;
    private boolean cacheLoadingNow;
    private boolean actualDataLoading;
    private boolean doLoadTabs;

    public FavePagesPresenter(int accountId, boolean isUser, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        pages = new ArrayList<>();
        faveInteractor = InteractorFactory.createFaveInteractor();
        this.isUser = isUser;
        searcher = new FindPage(actualDataDisposable);

        loadAllCachedData();
    }

    private void sleep_search(String q) {
        if (actualDataLoading || cacheLoadingNow) return;

        sleepDataDisposable.dispose();
        if (Utils.isEmpty(q)) {
            searcher.cancel();
        } else {
            if (!searcher.isSearchMode()) {
                searcher.insertCache(pages, pages.size());
            }
            sleepDataDisposable = (Single.just(new Object())
                    .delay(WEB_SEARCH_DELAY, TimeUnit.MILLISECONDS)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(videos -> searcher.do_search(q), this::onActualDataGetError));
        }
    }

    public void fireSearchRequestChanged(String q) {
        sleep_search(q == null ? null : q.trim());
    }

    @Override
    public void onGuiCreated(@NonNull IFaveUsersView view) {
        super.onGuiCreated(view);
        view.displayData(pages);
    }

    private void loadActualData(int offset) {
        actualDataLoading = true;

        resolveRefreshingView();

        int accountId = getAccountId();
        actualDataDisposable.add(faveInteractor.getPages(accountId, GET_COUNT, offset, isUser)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onActualDataReceived(offset, data), this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable t) {
        actualDataLoading = false;
        callView(v -> showError(v, getCauseIfRuntime(t)));

        resolveRefreshingView();
    }

    private void onActualDataReceived(int offset, List<FavePage> data) {
        cacheDisposable.clear();
        cacheLoadingNow = false;

        actualDataLoading = false;
        endOfContent = Utils.safeCountOf(data) < GET_COUNT;
        actualDataReceived = true;

        if (offset == 0) {
            pages.clear();
            pages.addAll(data);
            callView(IFaveUsersView::notifyDataSetChanged);
        } else {
            int startSize = pages.size();
            pages.addAll(data);
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
        callResumedView(v -> v.showRefreshing(actualDataLoading));
    }

    private void loadAllCachedData() {
        cacheLoadingNow = true;
        int accountId = getAccountId();

        cacheDisposable.add(faveInteractor.getCachedPages(accountId, isUser)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, this::onCachedGetError));
    }

    private void onCachedGetError(Throwable t) {
        callView(v -> showError(v, getCauseIfRuntime(t)));
    }

    private void onCachedDataReceived(List<FavePage> data) {
        cacheLoadingNow = false;

        pages.clear();
        pages.addAll(data);
        callView(IFaveUsersView::notifyDataSetChanged);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        actualDataDisposable.dispose();
        sleepDataDisposable.dispose();
        super.onDestroyed();
    }

    public void fireScrollToEnd() {
        if (nonEmpty(pages) && actualDataReceived && !cacheLoadingNow && !actualDataLoading) {
            if (searcher.isSearchMode()) {
                searcher.do_search();
            } else if (!endOfContent) {
                loadActualData(pages.size());
            }
        }
    }

    public void fireRefresh() {
        if (actualDataLoading || cacheLoadingNow) {
            return;
        }

        if (searcher.isSearchMode()) {
            searcher.reset();
        } else {
            loadActualData(0);
        }
    }

    public void fireOwnerClick(Owner owner) {
        callView(v -> v.openOwnerWall(getAccountId(), owner));
    }

    private void onUserRemoved(int accountId, int ownerId) {
        if (getAccountId() != accountId) {
            return;
        }

        int index = findIndexById(pages, Math.abs(ownerId));

        if (index != -1) {
            pages.remove(index);
            callView(view -> view.notifyItemRemoved(index));
        }
    }

    public void fireOwnerDelete(Owner owner) {
        int accountId = getAccountId();
        appendDisposable(faveInteractor.removePage(accountId, owner.getOwnerId(), isUser)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onUserRemoved(accountId, owner.getOwnerId()), t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
    }

    public void firePushFirst(Owner owner) {
        int accountId = getAccountId();
        appendDisposable(faveInteractor.pushFirst(accountId, owner.getOwnerId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(o -> fireRefresh(), t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
    }

    public void fireMention(Owner owner) {
        int accountId = getAccountId();
        callView(v -> v.openMention(accountId, owner));
    }

    private class FindPage extends FindAtWithContent<FavePage> {
        public FindPage(CompositeDisposable disposable) {
            super(disposable, SEARCH_VIEW_COUNT, SEARCH_COUNT);
        }

        @Override
        protected Single<List<FavePage>> search(int offset, int count) {
            return faveInteractor.getPages(getAccountId(), count, offset, isUser);
        }

        @Override
        protected void onError(@NonNull Throwable e) {
            onActualDataGetError(e);
        }

        @Override
        protected void onResult(@NonNull List<FavePage> data) {
            actualDataReceived = true;
            int startSize = pages.size();
            pages.addAll(data);
            callView(view -> view.notifyDataAdded(startSize, data.size()));
        }

        @Override
        protected void updateLoading(boolean loading) {
            actualDataLoading = loading;
            resolveRefreshingView();
        }

        @Override
        protected void clean() {
            pages.clear();
            callView(IFaveUsersView::notifyDataSetChanged);
        }

        @Override
        protected boolean compare(@NonNull FavePage data, @NonNull String q) {
            return Objects.nonNull(data.getOwner()) && Utils.safeCheck(data.getOwner().getFullName(), () -> data.getOwner().getFullName().toLowerCase().contains(q.toLowerCase()));
        }

        @Override
        protected void onReset(@NonNull List<FavePage> data, int offset, boolean isEnd) {
            if (Utils.isEmpty(data)) {
                fireRefresh();
            } else {
                pages.clear();
                pages.addAll(data);
                endOfContent = isEnd;
                callView(IFaveUsersView::notifyDataSetChanged);
            }
        }
    }
}
