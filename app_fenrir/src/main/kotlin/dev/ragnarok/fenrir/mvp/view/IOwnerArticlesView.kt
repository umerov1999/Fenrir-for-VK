package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Article
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IOwnerArticlesView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayData(articles: List<Article>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun goToArticle(accountId: Int, article: Article)
    fun goToPhoto(accountId: Int, photo: Photo)
}