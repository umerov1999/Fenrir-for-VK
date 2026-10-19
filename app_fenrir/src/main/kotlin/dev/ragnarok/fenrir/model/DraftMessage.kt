package dev.ragnarok.fenrir.model

class DraftMessage(val cmId: Int, val text: String?) {
    var attachmentsCount = 0
        private set

    fun setAttachmentsCount(attachmentsCount: Int) {
        this.attachmentsCount = attachmentsCount
    }

    override fun toString(): String {
        return "cmId=$cmId, text='$text', count=$attachmentsCount"
    }
}