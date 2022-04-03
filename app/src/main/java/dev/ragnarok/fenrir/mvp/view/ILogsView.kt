package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.LogEventType
import dev.ragnarok.fenrir.model.LogEventWrapper
import dev.ragnarok.fenrir.mvp.core.IMvpView

interface ILogsView : IMvpView, IErrorView {
    fun displayTypes(types: MutableList<LogEventType>)
    fun displayData(events: MutableList<LogEventWrapper>)
    fun showRefreshing(refreshing: Boolean)
    fun notifyEventDataChanged()
    fun notifyTypesDataChanged()
    fun setEmptyTextVisible(visible: Boolean)
}