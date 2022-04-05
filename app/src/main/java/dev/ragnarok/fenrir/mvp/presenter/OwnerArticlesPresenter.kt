package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.domain.IFaveInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.fromIOToMain
import dev.ragnarok.fenrir.model.Article
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.IOwnerArticlesView
import io.reactivex.rxjava3.disposables.CompositeDisposable

class OwnerArticlesPresenter(
    accountId: Int,
    private val ownerId: Int,
    savedInstanceState: Bundle?
) : AccountDependencyPresenter<IOwnerArticlesView>(accountId, savedInstanceState) {
    private val faveInteractor: IFaveInteractor = InteractorFactory.createFaveInteractor()
    private val mArticles: ArrayList<Article> = ArrayList()
    private val netDisposable = CompositeDisposable()
    private var mEndOfContent = false
    private var netLoadingNow = false
    private fun resolveRefreshingView() {
        view?.showRefreshing(
            netLoadingNow
        )
    }

    override fun onDestroyed() {
        netDisposable.dispose()
        super.onDestroyed()
    }

    private fun request(offset: Int) {
        netLoadingNow = true
        resolveRefreshingView()
        val accountId = accountId
        netDisposable.add(faveInteractor.getOwnerPublishedArticles(
            accountId,
            ownerId,
            COUNT_PER_REQUEST,
            offset
        )
            .fromIOToMain()
            .subscribe({ articles ->
                onNetDataReceived(
                    offset,
                    articles
                )
            }) { t -> onNetDataGetError(t) })
    }

    private fun onNetDataGetError(t: Throwable) {
        netLoadingNow = false
        resolveRefreshingView()
        showError(t)
    }

    private fun onNetDataReceived(offset: Int, articles: List<Article>) {
        mEndOfContent = articles.isEmpty()
        netLoadingNow = false
        if (offset == 0) {
            mArticles.clear()
            mArticles.addAll(articles)
            view?.notifyDataSetChanged()
        } else {
            val startSize = mArticles.size
            mArticles.addAll(articles)
            view?.notifyDataAdded(
                startSize,
                articles.size
            )
        }
        resolveRefreshingView()
    }

    private fun requestAtLast() {
        request(0)
    }

    private fun requestNext() {
        request(mArticles.size)
    }

    override fun onGuiCreated(viewHost: IOwnerArticlesView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(mArticles)
        resolveRefreshingView()
    }

    private fun canLoadMore(): Boolean {
        return mArticles.isNotEmpty() && !netLoadingNow && !mEndOfContent
    }

    fun fireRefresh() {
        netDisposable.clear()
        netLoadingNow = false
        requestAtLast()
    }

    fun fireArticleDelete(index: Int, article: Article) {
        appendDisposable(faveInteractor.removeArticle(accountId, article.ownerId, article.id)
            .fromIOToMain()
            .subscribe({
                mArticles[index].isFavorite = false
                view?.notifyDataSetChanged()
            }) { t -> onNetDataGetError(t) })
    }

    fun fireArticleAdd(index: Int, article: Article) {
        appendDisposable(faveInteractor.addArticle(accountId, article.url)
            .fromIOToMain()
            .subscribe({
                mArticles[index].isFavorite = true
                view?.notifyDataSetChanged()
            }) { t -> onNetDataGetError(t) })
    }

    fun fireArticleClick(url: String) {
        view?.goToArticle(
            accountId,
            url
        )
    }

    fun firePhotoClick(photo: Photo) {
        view?.goToPhoto(
            accountId,
            photo
        )
    }

    fun fireScrollToEnd() {
        if (canLoadMore()) {
            requestNext()
        }
    }

    companion object {
        private const val COUNT_PER_REQUEST = 25
    }

    init {
        requestAtLast()
    }
}