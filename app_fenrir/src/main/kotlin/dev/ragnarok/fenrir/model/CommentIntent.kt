package dev.ragnarok.fenrir.model

class CommentIntent(val authorId: Long) {
    var message: String? = null
        private set
    var replyToComment: Int? = null
        private set
    var draftMessageId: Int? = null
        private set
    var stickerId: Int? = null
        private set
    var attachmentCounts: Int = 0
        private set

    fun setAttachmentCounts(counts: Int): CommentIntent {
        attachmentCounts = counts
        return this
    }

    fun setDraftMessageId(draftMessageId: Int?): CommentIntent {
        this.draftMessageId = draftMessageId
        return this
    }

    fun setReplyToComment(replyToComment: Int?): CommentIntent {
        this.replyToComment = replyToComment
        return this
    }

    fun setStickerId(stickerId: Int?): CommentIntent {
        this.stickerId = stickerId
        return this
    }

    fun setMessage(message: String?): CommentIntent {
        this.message = message
        return this
    }
}