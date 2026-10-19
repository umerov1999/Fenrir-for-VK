package dev.ragnarok.fenrir.api.model.longpoll

class InputMessagesSetReadUpdate :
    AbsLongpollEvent(ACTION_SET_INPUT_MESSAGES_AS_READ) {
    var peerId = 0L
    var cmId = 0
    var unreadCount = 0

    fun set(peerId: Long, cmId: Int, unreadCount: Int): InputMessagesSetReadUpdate {
        this.peerId = peerId
        this.cmId = cmId
        this.unreadCount = unreadCount
        return this
    }
}