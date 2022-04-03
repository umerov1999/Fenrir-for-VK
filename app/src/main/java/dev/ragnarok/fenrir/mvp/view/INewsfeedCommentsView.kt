package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.NewsfeedComment
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface INewsfeedCommentsView : IAccountDependencyView, IAttachmentsPlacesView, IMvpView,
    IErrorView {
    fun displayData(data: List<NewsfeedComment>)
    fun notifyDataAdded(position: Int, count: Int)
    fun notifyDataSetChanged()
    fun showLoading(loading: Boolean)
}