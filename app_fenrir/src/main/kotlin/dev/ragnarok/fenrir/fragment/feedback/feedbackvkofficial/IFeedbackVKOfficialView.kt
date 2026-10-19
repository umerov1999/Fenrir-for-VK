package dev.ragnarok.fenrir.fragment.feedback.feedbackvkofficial

import dev.ragnarok.fenrir.fragment.base.core.IErrorView
import dev.ragnarok.fenrir.fragment.base.core.IMvpView
import dev.ragnarok.fenrir.model.FeedbackVKOfficial

interface IFeedbackVKOfficialView : IMvpView, IErrorView {
    fun displayData(list: List<FeedbackVKOfficial>)
    fun setEmptyTextVisible(visible: Boolean)
    fun notifyDataAdded(position: Int, count: Int)
    fun notifyItemRemoved(position: Int)
    fun notifyDataSetChanges()
    fun showRefreshing(refreshing: Boolean)
    fun notifyMarkViewedNotifications()
}