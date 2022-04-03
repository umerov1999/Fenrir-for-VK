package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.Includes.logsStore
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.db.interfaces.ILogsStorage
import dev.ragnarok.fenrir.model.LogEvent
import dev.ragnarok.fenrir.model.LogEventType
import dev.ragnarok.fenrir.model.LogEventWrapper
import dev.ragnarok.fenrir.mvp.presenter.base.RxSupportPresenter
import dev.ragnarok.fenrir.mvp.view.ILogsView
import dev.ragnarok.fenrir.util.DisposableHolder
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime

class LogsPresenter(savedInstanceState: Bundle?) :
    RxSupportPresenter<ILogsView>(savedInstanceState) {
    private val types: MutableList<LogEventType> = createTypes()
    private val events: MutableList<LogEventWrapper>
    private val store: ILogsStorage = logsStore
    private val disposableHolder = DisposableHolder<Int>()
    private var loadingNow = false
    private fun resolveEmptyTextVisibility() {
        view?.setEmptyTextVisible(events.isEmpty())
    }

    private fun setLoading(loading: Boolean) {
        loadingNow = loading
        resolveRefreshingView()
    }

    override fun onGuiCreated(viewHost: ILogsView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(events)
        viewHost.displayTypes(types)
        resolveEmptyTextVisibility()
    }

    public override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshingView()
    }

    private fun resolveRefreshingView() {
        resumedView?.showRefreshing(loadingNow)
    }

    fun fireClear() {
        store.Clear()
        loadAll()
    }

    private fun loadAll() {
        val type = selectedType
        setLoading(true)
        disposableHolder.append(store.getAll(type)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ events: List<LogEvent> -> onDataReceived(events) }) { throwable: Throwable? ->
                onDataReceiveError(
                    getCauseIfRuntime(throwable)
                )
            })
    }

    private fun onDataReceiveError(throwable: Throwable) {
        setLoading(false)
        view?.showError(throwable.message)
    }

    private fun onDataReceived(events: List<LogEvent>) {
        setLoading(false)
        this.events.clear()
        for (event in events) {
            this.events.add(LogEventWrapper(event))
        }
        view?.notifyEventDataChanged()
        resolveEmptyTextVisibility()
    }

    private val selectedType: Int
        get() {
            var type = LogEvent.Type.ERROR
            for (t in types) {
                if (t.isActive) {
                    type = t.type
                }
            }
            return type
        }

    override fun onDestroyed() {
        disposableHolder.dispose()
        super.onDestroyed()
    }

    fun fireTypeClick(entry: LogEventType) {
        if (selectedType == entry.type) {
            return
        }
        for (t in types) {
            t.setActive(t.type == entry.type)
        }
        view?.notifyTypesDataChanged()
        loadAll()
    }

    fun fireRefresh() {
        loadAll()
    }

    companion object {
        private fun createTypes(): MutableList<LogEventType> {
            val types: MutableList<LogEventType> = ArrayList()
            types.add(LogEventType(LogEvent.Type.ERROR, R.string.log_type_error).setActive(true))
            return types
        }
    }

    init {
        events = ArrayList()
        loadAll()
    }
}