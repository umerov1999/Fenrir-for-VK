package dev.ragnarok.fenrir.mvp.view.search

import dev.ragnarok.fenrir.model.Message

interface IMessagesSearchView : IBaseSearchView<Message> {
    fun goToMessagesLookup(accountId: Int, peerId: Int, messageId: Int)
}