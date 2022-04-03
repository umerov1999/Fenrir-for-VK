package dev.ragnarok.fenrir.mvp.presenter.search

import android.os.Bundle
import dev.ragnarok.fenrir.domain.IMessagesRepository
import dev.ragnarok.fenrir.domain.Repository.messages
import dev.ragnarok.fenrir.fragment.search.criteria.DialogsSearchCriteria
import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom
import dev.ragnarok.fenrir.model.Conversation
import dev.ragnarok.fenrir.model.Peer
import dev.ragnarok.fenrir.mvp.view.search.IDialogsSearchView
import dev.ragnarok.fenrir.trimmedNonNullNoEmpty
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.Pair.Companion.create
import io.reactivex.rxjava3.core.Single

class DialogsSearchPresenter(
    accountId: Int,
    criteria: DialogsSearchCriteria?,
    savedInstanceState: Bundle?
) : AbsSearchPresenter<IDialogsSearchView, DialogsSearchCriteria, Conversation, IntNextFrom>(
    accountId,
    criteria,
    savedInstanceState
) {
    private val messagesInteractor: IMessagesRepository = messages
    override val initialNextFrom: IntNextFrom
        get() = IntNextFrom(0)

    override fun isAtLast(startFrom: IntNextFrom): Boolean {
        return startFrom.offset == 0
    }

    override fun doSearch(
        accountId: Int,
        criteria: DialogsSearchCriteria,
        startFrom: IntNextFrom
    ): Single<Pair<List<Conversation>, IntNextFrom>> {
        val offset = startFrom.offset
        val nextFrom = IntNextFrom(offset + 50)
        return messagesInteractor.searchConversations(accountId, 50, criteria.query)
            .map {
                create(
                    it,
                    nextFrom
                )
            }
        // null because load more not supported
    }

    override fun instantiateEmptyCriteria(): DialogsSearchCriteria {
        return DialogsSearchCriteria("")
    }

    override fun canSearch(criteria: DialogsSearchCriteria?): Boolean {
        return criteria?.query.trimmedNonNullNoEmpty()
    }

    fun fireEntryClick(o: Conversation) {
        val accountId = accountId
        val messagesOwnerId = accountId // todo Community dialogs search !!!
        val peer = Peer(Peer.fromOwnerId(o.id)).setTitle(o.title).setAvaUrl(o.maxSquareAvatar)
        view?.openChatWith(
            accountId,
            messagesOwnerId,
            peer
        )
    }

}