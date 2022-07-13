package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.*
import dev.ragnarok.fenrir.domain.ICommunitiesInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.model.Community
import dev.ragnarok.fenrir.model.DataWrapper
import dev.ragnarok.fenrir.model.Owner
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.ICommunitiesView
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.Objects.safeEquals
import dev.ragnarok.fenrir.util.Translit.cyr2lat
import dev.ragnarok.fenrir.util.Translit.lat2cyr
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime
import dev.ragnarok.fenrir.util.Utils.indexOfOwner
import dev.ragnarok.fenrir.util.rxutils.RxUtils.ignore
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.*
import java.util.concurrent.TimeUnit

class CommunitiesPresenter(accountId: Int, private val userId: Int, savedInstanceState: Bundle?) :
    AccountDependencyPresenter<ICommunitiesView>(accountId, savedInstanceState) {
    private val own: DataWrapper<Community> = DataWrapper(ArrayList(), true)
    private val filtered: DataWrapper<Community> = DataWrapper(ArrayList(0), false)
    private val search: DataWrapper<Community> = DataWrapper(ArrayList(0), false)
    private val communitiesInteractor: ICommunitiesInteractor =
        InteractorFactory.createCommunitiesInteractor()
    private val actualDisposable = CompositeDisposable()
    private val cacheDisposable = CompositeDisposable()
    private val netSearchDisposable = CompositeDisposable()
    private val filterDisposable = CompositeDisposable()
    private val isNotFriendShow: Boolean =
        Settings.get().other().isOwnerInChangesMonitor(userId) && userId != accountId
    private var actualEndOfContent = false
    private var netSearchEndOfContent = false
    private var actualLoadingNow = false

    //private int actualLoadingOffset;
    private var cacheLoadingNow = false
    private var netSearchNow = false
    private var filter: String? = null
    private var not_communities: MutableList<Owner>? = null
    private var add_communities: MutableList<Owner>? = null
    private fun requestActualData(offset: Int, do_scan: Boolean) {
        actualLoadingNow = true
        //this.actualLoadingOffset = offset;
        val accountId = accountId
        resolveRefreshing()
        actualDisposable.add(communitiesInteractor.getActual(
            accountId,
            userId,
            if (isNotFriendShow) 1000 else 200,
            offset
        )
            .fromIOToMain()
            .subscribe({ communities ->
                onActualDataReceived(
                    offset,
                    communities,
                    do_scan
                )
            }) { t -> onActualDataGetError(t) })
    }

    public override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshing()
    }

    //private int netSearchOffset;
    private fun resolveRefreshing() {
        resumedView?.displayRefreshing(
            actualLoadingNow || netSearchNow
        )
    }

    private fun onActualDataGetError(t: Throwable) {
        actualLoadingNow = false
        resolveRefreshing()
        showError(t)
    }

    override fun onGuiCreated(viewHost: ICommunitiesView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(own, filtered, search)
        checkAndShowModificationCommunities()
    }

    private fun checkAndShowModificationCommunities() {
        if (add_communities.nonNullNoEmpty() || !not_communities.nonNullNoEmpty()) {
            view?.showModCommunities(
                add_communities,
                not_communities,
                accountId
            )
        }
    }

    fun clearModificationCommunities(add: Boolean, not: Boolean) {
        if (add && !add_communities.isNullOrEmpty()) {
            add_communities?.clear()
            add_communities = null
        }
        if (not && !not_communities.isNullOrEmpty()) {
            not_communities?.clear()
            not_communities = null
        }
    }

    private fun onActualDataReceived(offset: Int, communities: List<Community>, do_scan: Boolean) {
        //reset cache loading
        cacheDisposable.clear()
        cacheLoadingNow = false
        actualLoadingNow = false
        actualEndOfContent = communities.isEmpty()
        if (do_scan && isNotFriendShow) {
            not_communities = ArrayList()
            for (i in own.get()) {
                if (indexOfOwner(communities, i.ownerId) == -1) {
                    not_communities?.add(i)
                }
            }
            add_communities = ArrayList()
            for (i in communities) {
                if (indexOfOwner(own.get(), i.ownerId) == -1) {
                    add_communities?.add(i)
                }
            }
            checkAndShowModificationCommunities()
        }
        if (offset == 0) {
            own.get().clear()
            own.get().addAll(communities)
            view?.notifyDataSetChanged()
        } else {
            val startOwnSize = own.size()
            own.get().addAll(communities)
            view?.notifyOwnDataAdded(
                startOwnSize,
                communities.size
            )
        }
        resolveRefreshing()
    }

    private fun loadCachedData() {
        cacheLoadingNow = true
        val accountId = accountId
        cacheDisposable.add(communitiesInteractor.getCachedData(accountId, userId)
            .fromIOToMain()
            .subscribe({ communities -> onCachedDataReceived(communities) }) { t ->
                onCacheGetError(
                    t
                )
            })
    }

    private val isSearchNow: Boolean
        get() = filter.trimmedNonNullNoEmpty()

    private fun onCacheGetError(t: Throwable) {
        cacheLoadingNow = false
        showError(t)
        if (isNotFriendShow) {
            requestActualData(0, false)
        }
    }

    private fun onCachedDataReceived(communities: List<Community>) {
        cacheLoadingNow = false
        own.get().clear()
        own.get().addAll(communities)
        view?.notifyDataSetChanged()
        if (isNotFriendShow) {
            requestActualData(0, communities.isNotEmpty())
        }
    }

    fun fireSearchQueryChanged(query: String?) {
        if (!safeEquals(filter, query)) {
            filter = query
            onFilterChanged()
        }
    }

    private fun onFilterChanged() {
        val searchNow = filter.trimmedNonNullNoEmpty()
        own.isEnabled = !searchNow
        filtered.isEnabled = searchNow
        filtered.clear()
        search.isEnabled = searchNow
        search.clear()
        view?.notifyDataSetChanged()
        filterDisposable.clear()
        netSearchDisposable.clear()
        //netSearchOffset = 0;
        netSearchNow = false
        if (searchNow) {
            filterDisposable.add(
                filter(own.get(), filter)
                    .fromIOToMainComputation()
                    .subscribe({ filteredData ->
                        onFilteredDataReceived(
                            filteredData
                        )
                    }, ignore())
            )
            startNetSearch(0, true)
        } else {
            resolveRefreshing()
        }
    }

    private fun startNetSearch(offset: Int, withDelay: Boolean) {
        val accountId = accountId
        val filter = filter
        val single: Single<List<Community>>
        val searchSingle = communitiesInteractor.search(
            accountId, filter, null,
            null, null, null, 0, 100, offset
        )
        single = if (withDelay) {
            Completable.complete()
                .delay(1, TimeUnit.SECONDS)
                .andThen(searchSingle)
        } else {
            searchSingle
        }
        netSearchNow = true
        //this.netSearchOffset = offset;
        resolveRefreshing()
        netSearchDisposable.add(single
            .fromIOToMain()
            .subscribe({ data ->
                onSearchDataReceived(
                    offset,
                    data
                )
            }) { t -> onSearchError(t) })
    }

    private fun onSearchError(t: Throwable) {
        netSearchNow = false
        resolveRefreshing()
        showError(getCauseIfRuntime(t))
    }

    private fun onSearchDataReceived(offset: Int, communities: List<Community>) {
        netSearchNow = false
        netSearchEndOfContent = communities.isEmpty()
        resolveRefreshing()
        if (offset == 0) {
            search.replace(communities)
            view?.notifyDataSetChanged()
        } else {
            val sizeBefore = search.size()
            val count = communities.size
            search.addAll(communities)
            view?.notifySearchDataAdded(
                sizeBefore,
                count
            )
        }
    }

    private fun onFilteredDataReceived(filteredData: List<Community>) {
        filtered.replace(filteredData)
        view?.notifyDataSetChanged()
    }

    fun fireCommunityClick(community: Community) {
        view?.showCommunityWall(
            accountId,
            community
        )
    }

    fun fireUnsubscribe(community: Community) {
        actualDisposable.add(communitiesInteractor.leave(accountId, community.id)
            .fromIOToMain()
            .subscribe({ fireRefresh() }) { t -> onSearchError(t) })
    }

    fun fireCommunityLongClick(community: Community): Boolean {
        if ((exist(own, community) || exist(filtered, community)) && userId == accountId) {
            view?.showCommunityMenu(
                community
            )
            return true
        }
        return false
    }

    override fun onDestroyed() {
        actualDisposable.dispose()
        cacheDisposable.dispose()
        filterDisposable.dispose()
        netSearchDisposable.dispose()
        super.onDestroyed()
    }

    fun fireRefresh() {
        if (isSearchNow) {
            netSearchDisposable.clear()
            netSearchNow = false
            startNetSearch(0, false)
        } else {
            cacheDisposable.clear()
            cacheLoadingNow = false
            actualDisposable.clear()
            actualLoadingNow = false
            //actualLoadingOffset = 0;
            requestActualData(0, false)
        }
    }

    fun fireScrollToEnd() {
        if (isSearchNow) {
            if (!netSearchNow && !netSearchEndOfContent) {
                val offset = search.size()
                startNetSearch(offset, false)
            }
        } else {
            if (!actualLoadingNow && !cacheLoadingNow && !actualEndOfContent) {
                val offset = own.size()
                requestActualData(offset, false)
            }
        }
    }

    companion object {
        private fun filter(orig: List<Community>, filter: String?): Single<List<Community>> {
            return Single.create { emitter: SingleEmitter<List<Community>> ->
                val result: MutableList<Community> = ArrayList(5)
                for (community in orig) {
                    if (emitter.isDisposed) {
                        break
                    }
                    if (isMatchFilter(community, filter)) {
                        result.add(community)
                    }
                }
                emitter.onSuccess(result)
            }
        }

        private fun isMatchFilter(community: Community, filter: String?): Boolean {
            if (filter.trimmedIsNullOrEmpty()) {
                return true
            }
            val lower = filter.lowercase(Locale.getDefault()).trim { it <= ' ' }
            community.fullName.nonNullNoEmpty {
                val lowername = it.lowercase(Locale.getDefault())
                if (lowername.contains(lower)) {
                    return true
                }
                try {
                    val t = cyr2lat(lower)
                    if (t != null && lowername.contains(t)) {
                        return true
                    }
                } catch (ignored: Exception) {
                }
                try {
                    val t = lat2cyr(lower)
                    //Caused by java.lang.StringIndexOutOfBoundsException: length=3; index=3
                    if (t != null && lowername.contains(t)) {
                        return true
                    }
                } catch (ignored: Exception) {
                }
            }
            return community.domain.nonNullNoEmpty() && community.domain?.lowercase(Locale.getDefault())
                ?.contains(lower) == true
        }

        private fun exist(data: DataWrapper<Community>?, `in`: Community?): Boolean {
            if (data == null || `in` == null) {
                return false
            }
            for (i in 0 until data.size()) {
                if (data.get()[i].ownerId == `in`.ownerId) {
                    return true
                }
            }
            return false
        }
    }

    init {
        loadCachedData()
        if (!isNotFriendShow) {
            requestActualData(0, false)
        }
    }
}