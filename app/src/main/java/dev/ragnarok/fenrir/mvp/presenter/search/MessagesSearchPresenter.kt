package dev.ragnarok.fenrir.mvp.presenter.search

import android.os.Bundle
import dev.ragnarok.fenrir.domain.IMessagesRepository
import dev.ragnarok.fenrir.domain.Repository.messages
import dev.ragnarok.fenrir.fragment.search.criteria.MessageSearchCriteria
import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom
import dev.ragnarok.fenrir.model.Message
import dev.ragnarok.fenrir.mvp.view.search.IMessagesSearchView
import dev.ragnarok.fenrir.trimmedNonNullNoEmpty
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.Pair.Companion.create
import io.reactivex.rxjava3.core.Single

class MessagesSearchPresenter(
    accountId: Int,
    criteria: MessageSearchCriteria?,
    savedInstanceState: Bundle?
) : AbsSearchPresenter<IMessagesSearchView, MessageSearchCriteria, Message, IntNextFrom>(
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
        criteria: MessageSearchCriteria,
        startFrom: IntNextFrom
    ): Single<Pair<List<Message>, IntNextFrom>> {
        val offset = startFrom.offset
        return messagesInteractor
            .searchMessages(accountId, criteria.peerId, COUNT, offset, criteria.query)
            .map { messages -> create(messages, IntNextFrom(offset + COUNT)) }
    }

    override fun instantiateEmptyCriteria(): MessageSearchCriteria {
        return MessageSearchCriteria("")
    }

    override fun canSearch(criteria: MessageSearchCriteria?): Boolean {
        return criteria?.query.trimmedNonNullNoEmpty()
    }

    fun fireMessageClick(message: Message) {
        view?.goToMessagesLookup(
            accountId,
            message.peerId,
            message.id
        )
    }

    companion object {
        private const val COUNT = 50
    }

    init {
        if (canSearch(criteria)) {
            doSearch()
        }
    }
}