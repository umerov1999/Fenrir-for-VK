package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Poll
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface IPollView : IAccountDependencyView, IMvpView, IErrorView {
    fun displayQuestion(title: String?)
    fun displayPhoto(photo_url: String?)
    fun displayType(anonymous: Boolean)
    fun displayCreationTime(unixtime: Long)
    fun displayVoteCount(count: Int)
    fun displayVotesList(
        answers: MutableList<Poll.Answer>?,
        canCheck: Boolean,
        multiply: Boolean,
        checked: MutableSet<Int>
    )

    fun displayLoading(loading: Boolean)
    fun setupButton(voted: Boolean)

    fun openVoters(
        accountId: Int,
        ownerId: Int,
        pollId: Int,
        board: Boolean,
        answer: Int
    )
}