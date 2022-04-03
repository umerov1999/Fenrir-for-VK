package dev.ragnarok.fenrir.mvp.presenter

import android.content.Context
import android.os.Bundle
import android.text.InputType
import dev.ragnarok.fenrir.Includes.provideMainThreadScheduler
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.db.model.PostUpdate
import dev.ragnarok.fenrir.domain.*
import dev.ragnarok.fenrir.model.*
import dev.ragnarok.fenrir.mvp.presenter.base.PlaceSupportPresenter
import dev.ragnarok.fenrir.mvp.view.IFeedView
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.CustomToast.Companion.CreateCustomToast
import dev.ragnarok.fenrir.util.DisposableHolder
import dev.ragnarok.fenrir.util.InputTextDialog
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.RxUtils.applyCompletableIOToMainSchedulers
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers
import dev.ragnarok.fenrir.util.RxUtils.ignore
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime
import dev.ragnarok.fenrir.util.Utils.needReloadNews

class FeedPresenter(accountId: Int, savedInstanceState: Bundle?) :
    PlaceSupportPresenter<IFeedView>(accountId, savedInstanceState) {
    private val feedInteractor: IFeedInteractor
    private val faveInteractor: IFaveInteractor = InteractorFactory.createFaveInteractor()
    private val walls: IWallsRepository = Repository.walls
    private val mFeed: MutableList<News>
    private val mFeedSources: MutableList<FeedSource>
    private val loadingHolder = DisposableHolder<Void>()
    private val cacheLoadingHolder = DisposableHolder<Void>()
    private var mNextFrom: String? = null
    private var mSourceIds: String? = null
    private var loadingNow = false
    private var loadingNowNextFrom: String? = null
    private var cacheLoadingNow = false
    private var mTmpFeedScrollOnGuiReady: String? = null
    private fun refreshFeedSources() {
        val accountId = accountId
        appendDisposable(feedInteractor.getCachedFeedLists(accountId)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ lists: List<FeedList> ->
                onFeedListsUpdated(lists)
                requestActualFeedLists()
            }) { requestActualFeedLists() })
    }

    private fun requestActualFeedLists() {
        val accountId = accountId
        appendDisposable(
            feedInteractor.getActualFeedLists(accountId)
                .compose(applySingleIOToMainSchedulers())
                .subscribe({ lists: List<FeedList> -> onFeedListsUpdated(lists) }, ignore())
        )
    }

    private fun onPostUpdateEvent(update: PostUpdate) {
        if (update.likeUpdate != null) {
            val like = update.likeUpdate
            val index = indexOf(update.ownerId, update.postId)
            if (index != -1) {
                mFeed[index].likeCount = like.count
                mFeed[index].isUserLike = like.isLiked
                view?.notifyItemChanged(index)
            }
        }
    }

    private fun requestFeedAtLast(startFrom: String?) {
        loadingHolder.dispose()
        val accountId = accountId
        val sourcesIds = mSourceIds
        loadingNowNextFrom = startFrom
        loadingNow = true
        resolveLoadMoreFooterView()
        resolveRefreshingView()
        if ("updates" == sourcesIds) {
            loadingHolder.append(feedInteractor.getActualFeed(
                accountId,
                25,
                startFrom,
                "photo,photo_tag,wall_photo,friend,audio,video",
                9,
                sourcesIds
            )
                .compose(applySingleIOToMainSchedulers())
                .subscribe({ pair: Pair<List<News>, String?> ->
                    onActualFeedReceived(
                        startFrom,
                        pair.first,
                        pair.second
                    )
                }) { t: Throwable -> onActualFeedGetError(t) })
        } else {
            loadingHolder.append(feedInteractor.getActualFeed(
                accountId,
                25,
                startFrom,
                if (sourcesIds.isNullOrEmpty()) "post" else null,
                9,
                sourcesIds
            )
                .compose(applySingleIOToMainSchedulers())
                .subscribe({ pair: Pair<List<News>, String?> ->
                    onActualFeedReceived(
                        startFrom,
                        pair.first,
                        pair.second
                    )
                }) { t: Throwable -> onActualFeedGetError(t) })
        }
    }

    private fun onActualFeedGetError(t: Throwable) {
        loadingNow = false
        loadingNowNextFrom = null
        resolveLoadMoreFooterView()
        resolveRefreshingView()
        showError(t)
    }

    private fun onActualFeedReceived(startFrom: String?, feed: List<News>, nextFrom: String?) {
        loadingNow = false
        loadingNowNextFrom = null
        mNextFrom = nextFrom
        if (startFrom.isNullOrEmpty()) {
            mFeed.clear()
            mFeed.addAll(feed)
            view?.notifyFeedDataChanged()
        } else {
            val startSize = mFeed.size
            mFeed.addAll(feed)
            view?.notifyDataAdded(
                startSize,
                feed.size
            )
        }
        resolveRefreshingView()
        resolveLoadMoreFooterView()
    }

    override fun onGuiCreated(viewHost: IFeedView) {
        super.onGuiCreated(viewHost)
        viewHost.displayFeedSources(mFeedSources)
        val sourceIndex = activeFeedSourceIndex
        if (sourceIndex != -1) {
            viewHost.scrollFeedSourcesToPosition(sourceIndex)
        }
        viewHost.displayFeed(mFeed, mTmpFeedScrollOnGuiReady)
        mTmpFeedScrollOnGuiReady = null
        resolveRefreshingView()
        resolveLoadMoreFooterView()
    }

    private fun setCacheLoadingNow(cacheLoadingNow: Boolean) {
        this.cacheLoadingNow = cacheLoadingNow
        resolveRefreshingView()
        resolveLoadMoreFooterView()
    }

    private fun loadCachedFeed(thenScrollToState: String?) {
        val accountId = accountId
        setCacheLoadingNow(true)
        cacheLoadingHolder.append(
            feedInteractor
                .getCachedFeed(accountId)
                .compose(applySingleIOToMainSchedulers())
                .subscribe(
                    { feed: List<News> -> onCachedFeedReceived(feed, thenScrollToState) },
                    ignore()
                )
        )
    }

    override fun onDestroyed() {
        loadingHolder.dispose()
        cacheLoadingHolder.dispose()
        super.onDestroyed()
    }

    private fun onCachedFeedReceived(data: List<News>, thenScrollToState: String?) {
        setCacheLoadingNow(false)
        mFeed.clear()
        mFeed.addAll(data)
        if (thenScrollToState != null) {
            if (guiIsReady) {
                view?.displayFeed(
                    mFeed,
                    thenScrollToState
                )
            } else {
                mTmpFeedScrollOnGuiReady = thenScrollToState
            }
        } else {
            view?.notifyFeedDataChanged()
        }
        if (mFeed.isEmpty()) {
            requestFeedAtLast(null)
        } else {
            if (needReloadNews(accountId)) {
                val vr = Settings.get().main().start_newsMode
                if (vr == 2) {
                    view?.askToReload()
                } else if (vr == 1) {
                    view?.scrollTo(0)
                    requestFeedAtLast(null)
                }
            }
        }
    }

    private fun canLoadNextNow(): Boolean {
        return mNextFrom.nonNullNoEmpty() && !cacheLoadingNow && !loadingNow
    }

    private fun onFeedListsUpdated(lists: List<FeedList>) {
        val sources: MutableList<FeedSource> = ArrayList(lists.size)
        for (list in lists) {
            sources.add(FeedSource("list" + list.id, list.title, true))
        }
        mFeedSources.clear()
        mFeedSources.addAll(createDefaultFeedSources())
        mFeedSources.addAll(sources)
        val selected = refreshFeedSourcesSelection()
        view?.notifyFeedSourcesChanged()
        if (selected != -1) {
            view?.scrollFeedSourcesToPosition(selected)
        }
    }

    private fun refreshFeedSourcesSelection(): Int {
        var result = -1
        for (i in mFeedSources.indices) {
            val source = mFeedSources[i]
            if (mSourceIds.isNullOrEmpty() && source.value.isNullOrEmpty()) {
                source.setActive(true)
                result = i
                continue
            }
            if (mSourceIds.nonNullNoEmpty() && source.value.nonNullNoEmpty() && mSourceIds == source.value) {
                source.setActive(true)
                result = i
                continue
            }
            source.setActive(false)
        }
        return result
    }

    private fun restoreNextFromAndFeedSources() {
        mSourceIds = Settings.get()
            .other()
            .getFeedSourceIds(accountId)
        mNextFrom = Settings.get()
            .other()
            .restoreFeedNextFrom(accountId)
    }

    private val isRefreshing: Boolean
        get() = cacheLoadingNow || loadingNow && loadingNowNextFrom.isNullOrEmpty()
    private val isMoreLoading: Boolean
        get() = loadingNow && loadingNowNextFrom.nonNullNoEmpty()

    private fun resolveRefreshingView() {
        view?.showRefreshing(isRefreshing)
    }

    private val activeFeedSourceIndex: Int
        get() {
            for (i in mFeedSources.indices) {
                if (mFeedSources[i].isActive) {
                    return i
                }
            }
            return -1
        }

    private fun resolveLoadMoreFooterView() {
        if (mFeed.isEmpty() || mNextFrom.isNullOrEmpty()) {
            view?.setupLoadMoreFooter(LoadMoreState.END_OF_LIST)
        } else if (isMoreLoading) {
            view?.setupLoadMoreFooter(LoadMoreState.LOADING)
        } else if (canLoadNextNow()) {
            view?.setupLoadMoreFooter(LoadMoreState.CAN_LOAD_MORE)
        } else {
            view?.setupLoadMoreFooter(LoadMoreState.END_OF_LIST)
        }
    }

    fun fireScrollStateOnPause(json: String?) {
        Settings.get()
            .other()
            .storeFeedScrollState(accountId, json)
    }

    fun fireRefresh() {
        cacheLoadingHolder.dispose()
        loadingHolder.dispose()
        loadingNow = false
        cacheLoadingNow = false
        requestFeedAtLast(null)
    }

    fun fireScrollToBottom() {
        if (canLoadNextNow()) {
            requestFeedAtLast(mNextFrom)
        }
    }

    fun fireLoadMoreClick() {
        if (canLoadNextNow()) {
            requestFeedAtLast(mNextFrom)
        }
    }

    fun fireAddToFaveList(context: Context, owners: ArrayList<Owner>?) {
        if (owners.isNullOrEmpty()) {
            return
        }
        val Ids: MutableList<Int> = ArrayList(owners.size)
        for (i in owners) {
            Ids.add(i.ownerId)
        }
        InputTextDialog.Builder(context)
            .setTitleRes(R.string.set_news_list_title)
            .setAllowEmpty(false)
            .setInputType(InputType.TYPE_CLASS_TEXT)
            .setCallback(object : InputTextDialog.Callback {
                override fun onChanged(newValue: String?) {
                    appendDisposable(feedInteractor.saveList(
                        accountId,
                        newValue?.trim { it <= ' ' },
                        Ids
                    )
                        .compose(applySingleIOToMainSchedulers())
                        .subscribe({
                            CreateCustomToast(context).showToastSuccessBottom(R.string.success)
                            requestActualFeedLists()
                        }) { i: Throwable? ->
                            showError(i)
                        })
                }
            })
            .show()
    }

    fun fireFeedSourceClick(entry: FeedSource) {
        mSourceIds = entry.value
        mNextFrom = null
        cacheLoadingHolder.dispose()
        loadingHolder.dispose()
        loadingNow = false
        cacheLoadingNow = false
        refreshFeedSourcesSelection()
        view?.notifyFeedSourcesChanged()
        requestFeedAtLast(null)
    }

    fun fireFeedSourceDelete(id: Int?) {
        appendDisposable(feedInteractor.deleteList(accountId, id)
            .compose(applySingleIOToMainSchedulers())
            .subscribe(ignore()) { t: Throwable? ->
                showError(t)
            })
    }

    fun fireNewsShareLongClick(news: News) {
        view?.goToReposts(
            accountId,
            news.type,
            news.sourceId,
            news.postId
        )
    }

    fun fireNewsLikeLongClick(news: News) {
        view?.goToLikes(
            accountId,
            news.type,
            news.sourceId,
            news.postId
        )
    }

    fun fireAddBookmark(ownerId: Int, postId: Int) {
        appendDisposable(faveInteractor.addPost(accountId, ownerId, postId, null)
            .compose(applyCompletableIOToMainSchedulers())
            .subscribe({ onPostAddedToBookmarks() }) { t: Throwable? ->
                showError(getCauseIfRuntime(t))
            })
    }

    private fun onPostAddedToBookmarks() {
        view?.showSuccessToast()
    }

    fun fireNewsCommentClick(news: News) {
        if ("post".equals(news.type, ignoreCase = true)) {
            view?.goToPostComments(
                accountId,
                news.postId,
                news.sourceId
            )
        }
    }

    fun fireBanClick(news: News) {
        appendDisposable(feedInteractor.addBan(accountId, setOf(news.sourceId))
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ fireRefresh() }) { t: Throwable -> onActualFeedGetError(t) })
    }

    fun fireIgnoreClick(news: News) {
        val type = if ("post" == news.type) "wall" else news.type
        appendDisposable(feedInteractor.ignoreItem(accountId, type, news.sourceId, news.postId)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ fireRefresh() }) { t: Throwable -> onActualFeedGetError(t) })
    }

    fun fireNewsBodyClick(news: News) {
        if ("post" == news.type) {
            val post = news.toPost()
            view?.openPost(accountId, post)
        }
    }

    fun fireNewsRepostClick(news: News) {
        if ("post" == news.type) {
            view?.repostPost(
                accountId,
                news.toPost()
            )
        }
    }

    fun fireLikeClick(news: News) {
        if ("post".equals(news.type, ignoreCase = true)) {
            val add = !news.isUserLike
            val accountId = accountId
            appendDisposable(
                walls.like(accountId, news.sourceId, news.postId, add)
                    .compose(applySingleIOToMainSchedulers())
                    .subscribe(ignore(), ignore())
            )
        }
    }

    private fun indexOf(sourceId: Int, postId: Int): Int {
        for (i in mFeed.indices) {
            if (mFeed[i].sourceId == sourceId && mFeed[i].postId == postId) {
                return i
            }
        }
        return -1
    }

    companion object {
        private fun createDefaultFeedSources(): List<FeedSource> {
            val data: MutableList<FeedSource> = ArrayList(8)
            data.add(FeedSource(null, R.string.news_feed, false))
            data.add(FeedSource("likes", R.string.likes_posts, false))
            data.add(FeedSource("updates", R.string.updates, false))
            data.add(FeedSource("friends", R.string.friends, false))
            data.add(FeedSource("groups", R.string.groups, false))
            data.add(FeedSource("pages", R.string.pages, false))
            data.add(FeedSource("following", R.string.subscriptions, false))
            data.add(FeedSource("recommendation", R.string.recommendation, false))
            return data
        }
    }

    init {
        appendDisposable(walls.observeMinorChanges()
            .observeOn(provideMainThreadScheduler())
            .subscribe { update: PostUpdate -> onPostUpdateEvent(update) })
        feedInteractor = InteractorFactory.createFeedInteractor()
        mFeed = ArrayList()
        mFeedSources = ArrayList(createDefaultFeedSources())
        refreshFeedSourcesSelection()
        restoreNextFromAndFeedSources()
        refreshFeedSources()
        val scrollState = Settings.get()
            .other()
            .restoreFeedScrollState(accountId)
        loadCachedFeed(scrollState)
    }
}