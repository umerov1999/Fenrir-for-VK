package dev.ragnarok.fenrir.db.model.entity

import androidx.annotation.Keep
import dev.ragnarok.fenrir.model.ChatAction
import dev.ragnarok.fenrir.model.MessageStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Keep
@Serializable
class MessageDboEntity : DboEntity {
    var cmId = 0
        private set
    var peerId = 0L
        private set
    var fromId = 0L
        private set
    var date: Long = 0
        private set
    var isOut = false
        private set
    var text: String? = null
        private set
    var isImportant = false
        private set
    var isDeleted = false
        private set
    var isDeletedForAll = false
        private set
    var keyboard: KeyboardEntity? = null
        private set
    var reactions: List<ReactionEntity>? = null
        private set
    var reactionId = 0
        private set
    var version = 0
        private set
    @MessageStatus
    var status = 0
        private set
    @ChatAction
    var action = 0
        private set
    var actionMemberId = 0L
        private set
    var actionEmail: String? = null
        private set
    var actionText: String? = null
        private set
    var photo50: String? = null
        private set
    var photo100: String? = null
        private set
    var photo200: String? = null
        private set
    var randomId: Long = 0
        private set
    var extras: Map<Int, String>? = null
        private set
    private var attachments: List<DboEntity>? = null
    var forwardMessages: List<MessageDboEntity>? = null
        private set
    var payload: String? = null
        private set
    var updateTime: Long = 0
        private set

    fun set(cmId: Int, peerId: Long, fromId: Long): MessageDboEntity {
        this.cmId = cmId
        this.peerId = peerId
        this.fromId = fromId
        return this
    }

    override fun getAttachmentKey(): String {
        return "${cmId}_${peerId}"
    }

    fun setUpdateTime(updateTime: Long): MessageDboEntity {
        this.updateTime = updateTime
        return this
    }

    fun setDeletedForAll(deletedForAll: Boolean): MessageDboEntity {
        isDeletedForAll = deletedForAll
        return this
    }

    fun setFromId(fromId: Long): MessageDboEntity {
        this.fromId = fromId
        return this
    }

    fun setDate(date: Long): MessageDboEntity {
        this.date = date
        return this
    }

    fun setOut(out: Boolean): MessageDboEntity {
        isOut = out
        return this
    }

    fun setText(text: String?): MessageDboEntity {
        this.text = text
        return this
    }

    fun setKeyboard(keyboard: KeyboardEntity?): MessageDboEntity {
        this.keyboard = keyboard
        return this
    }

    fun setImportant(important: Boolean): MessageDboEntity {
        isImportant = important
        return this
    }

    fun setDeleted(deleted: Boolean): MessageDboEntity {
        isDeleted = deleted
        return this
    }

    fun setStatus(status: Int): MessageDboEntity {
        this.status = status
        return this
    }

    fun setAction(action: Int): MessageDboEntity {
        this.action = action
        return this
    }

    fun setActionMemberId(actionMemberId: Long): MessageDboEntity {
        this.actionMemberId = actionMemberId
        return this
    }

    fun setActionEmail(actionEmail: String?): MessageDboEntity {
        this.actionEmail = actionEmail
        return this
    }

    fun setActionText(actionText: String?): MessageDboEntity {
        this.actionText = actionText
        return this
    }

    fun setPhoto50(photo50: String?): MessageDboEntity {
        this.photo50 = photo50
        return this
    }

    fun setPhoto100(photo100: String?): MessageDboEntity {
        this.photo100 = photo100
        return this
    }

    fun setPhoto200(photo200: String?): MessageDboEntity {
        this.photo200 = photo200
        return this
    }

    fun setRandomId(randomId: Long): MessageDboEntity {
        this.randomId = randomId
        return this
    }

    fun setExtras(extras: Map<Int, String>?): MessageDboEntity {
        this.extras = extras
        return this
    }

    fun setForwardMessages(forwardMessages: List<MessageDboEntity>?): MessageDboEntity {
        this.forwardMessages = forwardMessages
        return this
    }

    fun getAttachments(): List<DboEntity>? {
        return attachments
    }

    fun setAttachments(attachments: List<DboEntity>?): MessageDboEntity {
        this.attachments = attachments
        return this
    }

    fun setPayload(payload: String?): MessageDboEntity {
        this.payload = payload
        return this
    }

    fun setReactionId(reactionId: Int): MessageDboEntity {
        this.reactionId = reactionId
        return this
    }

    fun setVersion(version: Int): MessageDboEntity {
        this.version = version
        return this
    }

    fun setReactions(reactions: List<ReactionEntity>?): MessageDboEntity {
        this.reactions = reactions
        return this
    }
}
