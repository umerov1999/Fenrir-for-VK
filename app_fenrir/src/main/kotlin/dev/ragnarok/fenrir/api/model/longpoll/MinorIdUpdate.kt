package dev.ragnarok.fenrir.api.model.longpoll

class MinorIdUpdate :
    AbsLongpollEvent(ACTION_CHANGE_MINOR_ID) {
    var peerId = 0L
    var minorId = 0
}