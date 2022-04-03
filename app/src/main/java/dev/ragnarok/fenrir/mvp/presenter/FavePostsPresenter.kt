package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.Includes.provideMainThreadScheduler
import dev.ragnarok.fenrir.db.model.PostUpdate
import dev.ragnarok.fenrir.domain.IFaveInteractor
import dev.ragnarok.fenrir.domain.IWallsRepository
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.domain.Repository.walls
import dev.ragnarok.fenrir.model.Post
import dev.ragnarok.fenrir.mvp.presenter.base.PlaceSupportPresenter
import dev.ragnarok.fenrir.mvp.view.IFavePostsView
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers
import dev.ragnarok.fenrir.util.RxUtils.ignore
import dev.ragnarok.fenrir.util.Utils.findInfoByPredicate
import io.reactivex.rxjava3.disposables.CompositeDisposable

class FavePostsPresenter(accountId: Int, savedInstanceState: Bundle?) :
    PlaceSupportPresenter<IFavePostsView>(accountId, savedInstanceState) {
    private val posts: MutableList<Post>
    private val faveInteractor: IFaveInteractor
    private val wallInteractor: IWallsRepository
    private val cacheCompositeDisposable = CompositeDisposable()
    private var requestNow = false
    private var actualInfoReceived = false
    private var nextOffset = 0
    private var endOfContent = false
    private var doLoadTabs = false
    private fun onPostUpdate(update: PostUpdate) {
        // likes only
        if (update.likeUpdate == null) {
            return
        }
        val info = findInfoByPredicate(
            posts
        ) {
            it.vkid == update.postId && it.ownerId == update.ownerId
        }
        if (info != null) {
            val post = info.second
            if (accountId == update.accountId) {
                post.isUserLikes = update.likeUpdate.isLiked
            }
            post.likesCount = update.likeUpdate.count
            view?.notifyItemChanged(
                info.first
            )
        }
    }

    private fun setRequestNow(requestNow: Boolean) {
        this.requestNow = requestNow
        resolveRefreshingView()
    }

    public override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshingView()
        doLoadTabs = if (doLoadTabs) {
            return
        } else {
            true
        }
        requestActual(0)
    }

    private fun resolveRefreshingView() {
        resumedView?.showRefreshing(
            requestNow
        )
    }

    override fun onGuiCreated(viewHost: IFavePostsView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(posts)
    }

    private fun requestActual(offset: Int) {
        setRequestNow(true)
        val accountId = accountId
        val newOffset = offset + COUNT
        appendDisposable(faveInteractor.getPosts(accountId, COUNT, offset)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ posts: List<Post> ->
                onActualDataReceived(
                    offset,
                    newOffset,
                    posts
                )
            }) { throwable: Throwable -> onActualDataGetError(throwable) })
    }

    private fun onActualDataGetError(throwable: Throwable) {
        setRequestNow(false)
        showError(throwable)
    }

    private fun onActualDataReceived(offset: Int, newOffset: Int, data: List<Post>) {
        setRequestNow(false)
        nextOffset = newOffset
        endOfContent = data.isEmpty()
        actualInfoReceived = true
        if (offset == 0) {
            posts.clear()
            posts.addAll(data)
            view?.notifyDataSetChanged()
        } else {
            val sizeBefore = posts.size
            posts.addAll(data)
            view?.notifyDataAdded(
                sizeBefore,
                data.size
            )
        }
    }

    private fun loadCachedData() {
        val accountId = accountId
        cacheCompositeDisposable.add(faveInteractor.getCachedPosts(accountId)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ posts: List<Post> -> onCachedDataReceived(posts) }) { obj: Throwable -> obj.printStackTrace() })
    }

    private fun onCachedDataReceived(posts: List<Post>) {
        this.posts.clear()
        this.posts.addAll(posts)
        view?.notifyDataSetChanged()
    }

    override fun onDestroyed() {
        cacheCompositeDisposable.dispose()
        super.onDestroyed()
    }

    fun fireRefresh() {
        if (!requestNow) {
            requestActual(0)
        }
    }

    fun fireScrollToEnd() {
        if (posts.isNotEmpty() && actualInfoReceived && !requestNow && !endOfContent) {
            requestActual(nextOffset)
        }
    }

    fun fireLikeClick(post: Post) {
        val accountId = accountId
        appendDisposable(wallInteractor.like(accountId, post.ownerId, post.vkid, !post.isUserLikes)
            .compose(applySingleIOToMainSchedulers())
            .subscribe(ignore()) { t: Throwable -> onLikeError(t) })
    }

    fun firePostDelete(index: Int, post: Post) {
        appendDisposable(faveInteractor.removePost(accountId, post.ownerId, post.vkid)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({
                posts.removeAt(index)
                view?.notifyDataSetChanged()
            }) { throwable: Throwable -> onActualDataGetError(throwable) })
    }

    private fun onLikeError(t: Throwable) {
        showError(t)
    }

    companion object {
        private const val COUNT = 50
    }

    init {
        posts = ArrayList()
        faveInteractor = InteractorFactory.createFaveInteractor()
        wallInteractor = walls
        appendDisposable(wallInteractor.observeMinorChanges()
            .observeOn(provideMainThreadScheduler())
            .subscribe { update: PostUpdate -> onPostUpdate(update) })
        loadCachedData()
    }
}