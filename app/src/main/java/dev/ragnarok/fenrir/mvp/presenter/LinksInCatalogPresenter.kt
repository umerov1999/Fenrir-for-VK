package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.domain.IAudioInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.model.CatalogBlock
import dev.ragnarok.fenrir.model.Link
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.ILinksInCatalogView
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime
import io.reactivex.rxjava3.disposables.CompositeDisposable

class LinksInCatalogPresenter(
    accountId: Int,
    private val block_id: String,
    savedInstanceState: Bundle?
) :
    AccountDependencyPresenter<ILinksInCatalogView>(accountId, savedInstanceState) {
    private val audioInteractor: IAudioInteractor = InteractorFactory.createAudioInteractor()
    private val links: ArrayList<Link> = ArrayList()
    private val audioListDisposable = CompositeDisposable()
    private var actualReceived = false
    private var next_from: String? = null
    private var loadingNow = false
    private var endOfContent = false
    private var doAudioLoadTabs = false
    fun setLoadingNow(loadingNow: Boolean) {
        this.loadingNow = loadingNow
        resolveRefreshingView()
    }

    public override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshingView()
        doAudioLoadTabs = if (doAudioLoadTabs) {
            return
        } else {
            true
        }
        fireRefresh()
    }

    private fun resolveRefreshingView() {
        resumedView?.displayRefreshing(
            loadingNow
        )
    }

    fun requestList() {
        setLoadingNow(true)
        audioListDisposable.add(audioInteractor.getCatalogBlockById(accountId, block_id, next_from)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ data: CatalogBlock? -> onListReceived(data) }) { t: Throwable ->
                onListGetError(
                    t
                )
            })
    }

    private fun onListReceived(data: CatalogBlock?) {
        if (data == null || data.links.isNullOrEmpty()) {
            actualReceived = true
            setLoadingNow(false)
            endOfContent = true
            return
        }
        if (next_from.isNullOrEmpty()) {
            links.clear()
        }
        next_from = data.next_from
        endOfContent = next_from.isNullOrEmpty()
        actualReceived = true
        setLoadingNow(false)
        links.addAll(data.links)
        view?.notifyListChanged()
    }

    override fun onDestroyed() {
        audioListDisposable.dispose()
        super.onDestroyed()
    }

    private fun onListGetError(t: Throwable) {
        setLoadingNow(false)
        showError(getCauseIfRuntime(t))
    }

    fun fireRefresh() {
        audioListDisposable.clear()
        next_from = null
        requestList()
    }

    fun fireScrollToEnd() {
        if (actualReceived && !endOfContent) {
            requestList()
        }
    }

    override fun onGuiCreated(viewHost: ILinksInCatalogView) {
        super.onGuiCreated(viewHost)
        viewHost.displayList(links)
    }

}