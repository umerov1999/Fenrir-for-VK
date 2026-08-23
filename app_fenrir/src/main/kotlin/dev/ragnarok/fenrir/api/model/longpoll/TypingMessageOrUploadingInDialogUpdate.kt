package dev.ragnarok.fenrir.api.model.longpoll

class TypingMessageOrUploadingInDialogUpdate(action: Int) :
    AbsLongpollEvent(action) {
    var peer_id = 0L
    var from_ids: LongArray? = null
    var from_ids_count = 0
}