package dev.ragnarok.fenrir.api.model.longpoll

class PinConversationUpdate :
    AbsLongpollEvent(ACTION_PIN_CONVERSATION) {
    var peerId = 0L
    var majorId = 0
}