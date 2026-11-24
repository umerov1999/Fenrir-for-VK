package dev.ragnarok.fenrir.api.model.longpoll

class ReactionsUnreadInConversationUpdate(
    val peerId: Long,
    val messagesCount: Int,
    val conversationMessageIds: IntArray?
) : AbsLongpollEvent(ACTION_UPDATE_UNREAD_REACTIONS_IN_CONVERSATION)