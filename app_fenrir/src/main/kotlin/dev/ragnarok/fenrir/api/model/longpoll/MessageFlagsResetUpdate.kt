package dev.ragnarok.fenrir.api.model.longpoll

class MessageFlagsResetUpdate :
    AbsLongpollEvent(ACTION_MESSAGES_FLAGS_RESET) {
    var cmId = 0
    var mask = 0
    var peerId = 0L
}