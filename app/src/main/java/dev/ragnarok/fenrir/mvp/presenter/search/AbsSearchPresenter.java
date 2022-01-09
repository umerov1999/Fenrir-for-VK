package dev.ragnarok.fenrir.mvp.presenter.search;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.fragment.search.criteria.BaseSearchCriteria;
import dev.ragnarok.fenrir.fragment.search.nextfrom.AbsNextFrom;
import dev.ragnarok.fenrir.mvp.presenter.base.PlaceSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.search.IBaseSearchView;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.WeakActionHandler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public abstract class AbsSearchPresenter<V extends IBaseSearchView<T>, C extends BaseSearchCriteria, T, N extends AbsNextFrom> extends PlaceSupportPresenter<V> {

    private static final String SAVE_CRITERIA = "save_criteria";
    private static final int MESSAGE = 67;
    private static final int SEARCH_DELAY = 1500;

    final List<T> data;
    private final C criteria;
    private final WeakActionHandler<AbsSearchPresenter<?, ?, ?, ?>> actionHandler = new WeakActionHandler<>(this);
    private final CompositeDisposable searchDisposable = new CompositeDisposable();
    private N nextFrom;
    private C resultsForCriteria;
    private boolean endOfContent;
    private boolean loadingNow;

    AbsSearchPresenter(int accountId, @Nullable C criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        if (isNull(savedInstanceState)) {
            this.criteria = isNull(criteria) ? instantiateEmptyCriteria() : criteria;
        } else {
            this.criteria = savedInstanceState.getParcelable(SAVE_CRITERIA);
        }

        nextFrom = getInitialNextFrom();
        data = new ArrayList<>();
        actionHandler.setAction((what, object) -> object.doSearch());
    }

    @Override
    public void onGuiCreated(@NonNull V view) {
        super.onGuiCreated(view);

        // пробуем искать при первом создании view
        if (getViewCreationCount() == 1) {
            doSearch();
        }
        resolveListData();
        resolveEmptyText();
    }

    C getCriteria() {
        return criteria;
    }

    N getNextFrom() {
        return nextFrom;
    }

    abstract N getInitialNextFrom();

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelable(SAVE_CRITERIA, criteria);
    }

    @SuppressWarnings("unchecked")
    void doSearch() {
        if (!canSearch(criteria) || isNull(nextFrom)) {
            //setLoadingNow(false);
            return;
        }

        int accountId = getAccountId();
        C cloneCriteria = (C) criteria.safellyClone();
        N nf = nextFrom;

        setLoadingNow(true);
        searchDisposable.add(doSearch(accountId, cloneCriteria, nf)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(pair -> onSearchDataReceived(cloneCriteria, nf, pair.getFirst(), pair.getSecond()),
                        this::onSearchError));
    }

    void onSearchError(Throwable throwable) {
        throwable.printStackTrace();
    }

    abstract boolean isAtLast(N startFrom);

    private void onSearchDataReceived(C criteria, N startFrom, List<T> data, N nextFrom) {
        setLoadingNow(false);

        boolean clearPrevious = isAtLast(startFrom);

        this.nextFrom = nextFrom;
        resultsForCriteria = criteria;
        endOfContent = data.isEmpty();

        if (clearPrevious) {
            this.data.clear();
            this.data.addAll(data);
            callView(IBaseSearchView::notifyDataSetChanged);
        } else {
            int startSize = this.data.size();
            this.data.addAll(data);
            callView(view -> view.notifyDataAdded(startSize, data.size()));
        }

        resolveEmptyText();
    }

    public final void fireTextQueryEdit(String q) {
        criteria.setQuery(q);

        fireCriteriaChanged();
    }

    private void resolveListData() {
        callView(v -> v.displayData(data));
    }

    private void resolveEmptyText() {
        callView(v -> v.setEmptyTextVisible(data.isEmpty()));
    }

    private void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveLoadingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveLoadingView();
    }

    private void resolveLoadingView() {
        callResumedView(v -> v.showLoading(loadingNow));
    }

    private void fireCriteriaChanged() {
        if (criteria.equals(resultsForCriteria)) {
            return;
        }

        searchDisposable.clear();
        setLoadingNow(false);

        nextFrom = getInitialNextFrom();
        data.clear();

        resolveListData();
        resolveEmptyText();

        actionHandler.removeMessages(MESSAGE);

        if (canSearch(criteria)) {
            actionHandler.sendEmptyMessageDelayed(MESSAGE, SEARCH_DELAY);
        }
    }

    abstract Single<Pair<List<T>, N>> doSearch(int accountId, C criteria, N startFrom);

    abstract C instantiateEmptyCriteria();

    @Override
    public void onDestroyed() {
        actionHandler.setAction(null);
        searchDisposable.clear();
        super.onDestroyed();
    }

    abstract boolean canSearch(C criteria);

    public final void fireScrollToEnd() {
        if (canLoadMore()) {
            doSearch();
        }
    }

    private boolean canLoadMore() {
        return !endOfContent && !loadingNow && !data.isEmpty() && nonNull(nextFrom);
    }

    public void fireRefresh() {
        if (loadingNow || !canSearch(criteria)) {
            resolveLoadingView();
            return;
        }

        nextFrom = getInitialNextFrom();
        doSearch();
    }

    public void fireOptionsChanged() {
        fireCriteriaChanged();
    }

    public void fireOpenFilterClick() {
        callView(v -> v.displayFilter(getAccountId(), criteria.getOptions()));
    }
}