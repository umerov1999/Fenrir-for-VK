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
import dev.ragnarok.fenrir.mvp.view.IOwnerArticlesView;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class OwnerArticlesPresenter extends AccountDependencyPresenter<IOwnerArticlesView> {

    private static final int COUNT_PER_REQUEST = 25;
    private final IFaveInteractor faveInteractor;
    private final ArrayList<Article> mArticles;
    private final CompositeDisposable netDisposable = new CompositeDisposable();
    private final int ownerId;
    private boolean mEndOfContent;
    private boolean netLoadingNow;

    public OwnerArticlesPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.ownerId = ownerId;

        faveInteractor = InteractorFactory.createFaveInteractor();
        mArticles = new ArrayList<>();

        requestAtLast();
    }

    private void resolveRefreshingView() {
        callView(v -> v.showRefreshing(netLoadingNow));
    }

    @Override
    public void onDestroyed() {
        netDisposable.dispose();
        super.onDestroyed();
    }

    private void request(int offset) {
        netLoadingNow = true;
        resolveRefreshingView();

        int accountId = getAccountId();

        netDisposable.add(faveInteractor.getOwnerPublishedArticles(accountId, ownerId, COUNT_PER_REQUEST, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(articles -> onNetDataReceived(offset, articles), this::onNetDataGetError));
    }

    private void onNetDataGetError(Throwable t) {
        netLoadingNow = false;
        resolveRefreshingView();
        callView(v -> showError(v, t));
    }

    private void onNetDataReceived(int offset, List<Article> articles) {

        mEndOfContent = articles.isEmpty();
        netLoadingNow = false;

        if (offset == 0) {
            mArticles.clear();
            mArticles.addAll(articles);
            callView(IOwnerArticlesView::notifyDataSetChanged);
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
    public void onGuiCreated(@NonNull IOwnerArticlesView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(mArticles);

        resolveRefreshingView();
    }

    private boolean canLoadMore() {
        return !mArticles.isEmpty() && !netLoadingNow && !mEndOfContent;
    }

    public void fireRefresh() {
        netDisposable.clear();
        netLoadingNow = false;

        requestAtLast();
    }

    public void fireArticleDelete(int index, Article article) {
        appendDisposable(faveInteractor.removeArticle(getAccountId(), article.getOwnerId(), article.getId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(articles -> {
                    mArticles.get(index).setIsFavorite(false);
                    callView(IOwnerArticlesView::notifyDataSetChanged);
                }, this::onNetDataGetError));
    }

    public void fireArticleAdd(int index, Article article) {
        appendDisposable(faveInteractor.addArticle(getAccountId(), article.getURL())
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> {
                    mArticles.get(index).setIsFavorite(true);
                    callView(IOwnerArticlesView::notifyDataSetChanged);
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
