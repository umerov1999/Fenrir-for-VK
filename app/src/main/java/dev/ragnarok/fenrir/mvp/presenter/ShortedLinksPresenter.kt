package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.api.model.VKApiCheckedLink
import dev.ragnarok.fenrir.domain.IUtilsInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.model.ShortLink
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.IShortedLinksView
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers
import dev.ragnarok.fenrir.util.Unixtime.now
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime
import io.reactivex.rxjava3.disposables.CompositeDisposable

class ShortedLinksPresenter(accountId: Int, savedInstanceState: Bundle?) :
    AccountDependencyPresenter<IShortedLinksView>(accountId, savedInstanceState) {
    private val links: MutableList<ShortLink>
    private val fInteractor: IUtilsInteractor
    private val actualDataDisposable = CompositeDisposable()
    private var actualDataReceived = false
    private var endOfContent = false
    private var actualDataLoading = false
    private var mInput: String? = null
    override fun onGuiCreated(viewHost: IShortedLinksView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(links)
    }

    private fun loadActualData(offset: Int) {
        actualDataLoading = true
        resolveRefreshingView()
        val accountId = accountId
        actualDataDisposable.add(fInteractor.getLastShortenedLinks(accountId, 10, offset)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({
                onActualDataReceived(
                    offset,
                    it
                )
            }) { t: Throwable -> onActualDataGetError(t) })
    }

    private fun onActualDataGetError(t: Throwable) {
        actualDataLoading = false
        showError(getCauseIfRuntime(t))
        resolveRefreshingView()
    }

    private fun onActualDataReceived(offset: Int, data: List<ShortLink>) {
        actualDataLoading = false
        endOfContent = data.isEmpty()
        actualDataReceived = true
        if (offset == 0) {
            links.clear()
            links.addAll(data)
            view?.notifyDataSetChanged()
        } else {
            val startSize = links.size
            links.addAll(data)
            view?.notifyDataAdded(
                startSize,
                data.size
            )
        }
        resolveRefreshingView()
    }

    public override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshingView()
    }

    private fun resolveRefreshingView() {
        view?.showRefreshing(
            actualDataLoading
        )
    }

    override fun onDestroyed() {
        actualDataDisposable.dispose()
        super.onDestroyed()
    }

    fun fireScrollToEnd(): Boolean {
        if (!endOfContent && links.nonNullNoEmpty() && actualDataReceived && !actualDataLoading) {
            loadActualData(links.size)
            return false
        }
        return true
    }

    fun fireDelete(index: Int, link: ShortLink) {
        actualDataDisposable.add(fInteractor.deleteFromLastShortened(accountId, link.key)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({
                links.removeAt(index)
                view?.notifyDataSetChanged()
            }) { t: Throwable -> onActualDataGetError(t) })
    }

    fun fireRefresh() {
        actualDataDisposable.clear()
        actualDataLoading = false
        loadActualData(0)
    }

    fun fireInputEdit(s: CharSequence?) {
        mInput = s.toString()
    }

    fun fireShort() {
        actualDataDisposable.add(fInteractor.getShortLink(accountId, mInput, 1)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ data: ShortLink ->
                data.timestamp = now()
                data.views = 0
                links.add(0, data)
                view?.notifyDataSetChanged()
                view?.updateLink(
                    data.short_url
                )
            }) { t: Throwable -> onActualDataGetError(t) })
    }

    fun fireValidate() {
        actualDataDisposable.add(fInteractor.checkLink(accountId, mInput)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ data: VKApiCheckedLink ->
                view?.updateLink(
                    data.link
                )
                view?.showLinkStatus(
                    data.status
                )
            }) { t: Throwable -> onActualDataGetError(t) })
    }

    init {
        links = ArrayList()
        fInteractor = InteractorFactory.createUtilsInteractor()
        loadActualData(0)
    }
}