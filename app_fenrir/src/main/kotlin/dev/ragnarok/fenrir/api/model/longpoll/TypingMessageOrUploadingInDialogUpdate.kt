package dev.ragnarok.fenrir.api.model.longpoll

class TypingMessageOrUploadingInDialogUpdate(action: Int) :
    AbsLongpollEvent(action) {
    var peerId = 0L
    var fromIds: LongArray? = null
    var fromIdsCount = 0
}