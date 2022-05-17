package dev.ragnarok.fenrir.mvp.view.search

import dev.ragnarok.fenrir.fragment.search.options.BaseOption
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.IAttachmentsPlacesView
import dev.ragnarok.fenrir.mvp.view.IErrorView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IBaseSearchView<T> : IMvpView, IErrorView, IAccountDependencyView,
    IAttachmentsPlacesView {
    fun displayData(data: MutableList<T>)
    fun setEmptyTextVisible(visible: Boolean)
    fun notifyDataSetChanged()
    fun notifyItemChanged(index: Int)
    fun notifyDataAdded(position: Int, count: Int)
    fun showLoading(loading: Boolean)
    fun displayFilter(accountId: Int, options: ArrayList<BaseOption>)
}