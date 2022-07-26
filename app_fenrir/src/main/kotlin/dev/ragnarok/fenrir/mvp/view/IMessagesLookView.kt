package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.LoadMoreState
import dev.ragnarok.fenrir.model.Message

interface IMessagesLookView : IBasicMessageListView, IErrorView {
    fun focusTo(index: Int)
    fun setupHeaders(@LoadMoreState upHeaderState: Int, @LoadMoreState downHeaderState: Int)
    fun forwardMessages(accountId: Int, messages: ArrayList<Message>)
}