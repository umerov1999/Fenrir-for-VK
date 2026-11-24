package dev.ragnarok.fenrir.fragment.messages.conversationattachments.abschatattachments

import dev.ragnarok.fenrir.fragment.base.IAttachmentsPlacesView
import dev.ragnarok.fenrir.fragment.base.core.IErrorView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView
import dev.ragnarok.fenrir.fragment.base.core.IToastView

interface IBaseChatAttachmentsView<T> : IMvpView, IAttachmentsPlacesView, IToastView,
    IErrorView {
    fun displayAttachments(data: MutableList<T>)
    fun notifyDataAdded(position: Int, count: Int)
    fun notifyItemRemoved(index: Int)
    fun notifyItemChanged(index: Int)
    fun notifyDatasetChanged()
    fun showLoading(loading: Boolean)
    fun setEmptyTextVisible(visible: Boolean)
    fun setToolbarTitleString(title: String)
    fun setToolbarSubtitleString(subtitle: String)
}