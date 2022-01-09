package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.domain.IFaveInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Article;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IFaveArticlesView;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class FaveArticlesPresenter extends AccountDependencyPresenter<IFaveArticlesView> {

    private static final int COUNT_PER_REQUEST = 25;
    private final IFaveInteractor faveInteractor;
    private final ArrayList<Article> mArticles;
    private final CompositeDisposable cacheDisposable = new CompositeDisposable();
    private final CompositeDisposable netDisposable = new CompositeDisposable();
    private boolean mEndOfContent;
    private boolean cacheLoadingNow;
    private boolean netLoadingNow;
    private boolean doLoadTabs;

    public FaveArticlesPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        faveInteractor = InteractorFactory.createFaveInteractor();
        mArticles = new ArrayList<>();

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
        cacheDisposable.add(faveInteractor.getCachedArticles(accountId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, this::onCacheGetError));
    }

    private void onCacheGetError(Throwable t) {
        cacheLoadingNow = false;
        callView(v -> showError(v, t));
    }

    private void onCachedDataReceived(List<Article> articles) {
        cacheLoadingNow = false;

        mArticles.clear();
        mArticles.addAll(articles);
        callView(IFaveArticlesView::notifyDataSetChanged);
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

        netDisposable.add(faveInteractor.getArticles(accountId, COUNT_PER_REQUEST, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(articles -> onNetDataReceived(offset, articles), this::onNetDataGetError));
    }

    private void onNetDataGetError(Throwable t) {
        netLoadingNow = false;
        resolveRefreshingView();
        callView(v -> showError(v, t));
    }

    private void onNetDataReceived(int offset, List<Article> articles) {
        cacheDisposable.clear();
        cacheLoadingNow = false;

        mEndOfContent = articles.isEmpty();
        netLoadingNow = false;

        if (offset == 0) {
            mArticles.clear();
            mArticles.addAll(articles);
            callView(IFaveArticlesView::notifyDataSetChanged);
        } else {
            int startSize = mArticles.size();
            mArticles.addAll(articles);
            callView(view -> view.notifyDataAdded(startSize, articles.size()));
        }

        resolveRefreshingView();
    }

    private void requestAtLast() {
        request(0);
    }

    private void requestNext() {
        request(mArticles.size());
    }

    @Override
    public void onGuiCreated(@NonNull IFaveArticlesView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(mArticles);
    }

    private boolean canLoadMore() {
        return !mArticles.isEmpty() && !cacheLoadingNow && !netLoadingNow && !mEndOfContent;
    }

    public void fireRefresh() {
        cacheDisposable.clear();
        netDisposable.clear();
        netLoadingNow = false;

        requestAtLast();
    }

    public void fireArticleDelete(int index, Article article) {
        appendDisposable(faveInteractor.removeArticle(getAccountId(), article.getOwnerId(), article.getId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(videos -> {
                    mArticles.remove(index);
                    callView(IFaveArticlesView::notifyDataSetChanged);
                }, this::onNetDataGetError));
    }

    public void fireArticleClick(String url) {
        callView(v -> v.goToArticle(getAccountId(), url));
    }

    public void firePhotoClick(Photo photo) {
        callView(v -> v.goToPhoto(getAccountId(), photo));
    }

    public void fireScrollToEnd() {
        if (canLoadMore()) {
            requestNext();
        }
    }
}
