package dev.ragnarok.fenrir.mvp.view.conversations

import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.IAttachmentsPlacesView
import dev.ragnarok.fenrir.mvp.view.IErrorView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IBaseChatAttachmentsView<T> : IMvpView, IAccountDependencyView, IAttachmentsPlacesView,
    IErrorView {
    fun displayAttachments(data: MutableList<T>)
    fun notifyDataAdded(position: Int, count: Int)
    fun notifyDatasetChanged()
    fun showLoading(loading: Boolean)
    fun setEmptyTextVisible(visible: Boolean)
    fun setToolbarTitleString(title: String)
    fun setToolbarSubtitleString(subtitle: String)
}