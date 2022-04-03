package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.LoadMoreState
import dev.ragnarok.fenrir.model.Message
import dev.ragnarok.fenrir.model.Peer

interface INotReadMessagesView : IBasicMessageListView, IErrorView {
    fun focusTo(index: Int)
    fun setupHeaders(@LoadMoreState upHeaderState: Int, @LoadMoreState downHeaderState: Int)
    fun forwardMessages(accountId: Int, messages: ArrayList<Message>)
    fun doFinish(incoming: Int, outgoing: Int, notAnim: Boolean)
    fun displayToolbarAvatar(peer: Peer?)
    fun displayUnreadCount(unreadCount: Int)
}