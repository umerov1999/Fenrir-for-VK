package dev.ragnarok.fenrir.util

import dev.ragnarok.fenrir.api.model.*
import dev.ragnarok.fenrir.api.model.feedback.Copies
import dev.ragnarok.fenrir.api.model.feedback.UserArray
import dev.ragnarok.fenrir.api.model.feedback.VkApiUsersFeedback
import dev.ragnarok.fenrir.model.Message
import dev.ragnarok.fenrir.model.Peer
import dev.ragnarok.fenrir.nonNullNoEmpty
import kotlin.math.abs

class VKOwnIds {
    private val uids: MutableSet<Int>
    private val gids: MutableSet<Int>
    fun append(userArray: UserArray): VKOwnIds {
        for (id in userArray.ids) {
            append(id)
        }
        return this
    }

    fun appendStory(story: VKApiStory): VKOwnIds {
        append(story.owner_id)
        return this
    }

    fun append(dto: VkApiUsersFeedback): VKOwnIds {
        append(dto.users)
        return this
    }

    fun append(topic: VKApiTopic): VKOwnIds {
        append(topic.created_by)
        append(topic.updated_by)
        return this
    }

    fun append(copies: Copies): VKOwnIds {
        for (pair in copies.pairs) {
            append(pair.owner_id)
        }
        return this
    }

    fun append(commentsDto: CommentsDto?): VKOwnIds {
        if (commentsDto != null && commentsDto.list.nonNullNoEmpty()) {
            for (comment in commentsDto.list) {
                append(comment)
            }
        }
        return this
    }

    fun append(comment: VKApiComment): VKOwnIds {
        if (comment.from_id != 0) {
            append(comment.from_id)
        }
        if (comment.attachments != null) {
            append(comment.attachments)
        }
        if (comment.threads.nonNullNoEmpty()) {
            for (i in comment.threads) {
                append(i)
            }
        }
        return this
    }

    private fun appendAttachmentDto(attachment: VKApiAttachment?): VKOwnIds {
        if (attachment is VKApiPost) {
            append(attachment)
        } else if (attachment is VKApiStory) {
            appendStory(attachment)
        }
        return this
    }

    fun append(attachments: VkApiAttachments): VKOwnIds {
        val entries = attachments.entryList()
        for (entry in entries) {
            appendAttachmentDto(entry.attachment)
        }
        return this
    }

    val all: Collection<Int>
        get() {
            val result: MutableCollection<Int> = HashSet(uids.size + gids.size)
            result.addAll(uids)
            for (gid in gids) {
                result.add(-abs(gid))
            }
            return result
        }

    fun getUids(): Set<Int> {
        return uids
    }

    fun getGids(): Set<Int> {
        return gids
    }

    fun appendAll(ids: Collection<Int>): VKOwnIds {
        for (id in ids) {
            append(id)
        }
        return this
    }

    fun append(messages: Collection<VKApiMessage>?): VKOwnIds {
        if (messages != null) {
            for (message in messages) {
                append(message)
            }
        }
        return this
    }

    fun append(dialog: VkApiDialog): VKOwnIds {
        if (dialog.lastMessage != null) {
            append(dialog.lastMessage)
        }
        return this
    }

    fun append(conversation: VkApiConversation): VKOwnIds {
        if (!Peer.isGroupChat(conversation.peer.id)) {
            append(conversation.peer.id)
        }
        return this
    }

    fun append(message: VKApiMessage): VKOwnIds {
        append(message.from_id)
        append(message.action_mid)
        if (!message.isGroupChat) {
            append(message.peer_id)
        }
        if (message.fwd_messages != null) {
            for (fwd in message.fwd_messages) {
                append(fwd)
            }
        }
        if (message.attachments != null) {
            val entries = message.attachments.entryList()
            for (entry in entries) {
                if (entry.attachment is VKApiPost) {
                    append(entry.attachment)
                } else if (entry.attachment is VKApiStory) {
                    appendStory(entry.attachment)
                }
            }
        }
        return this
    }

    fun append(messages: ArrayList<Message>): VKOwnIds {
        for (message in messages) {
            append(message)
        }
        return this
    }

    fun append(message: Message): VKOwnIds {
        append(message.senderId)
        append(message.actionMid) // тут 100% пользователь, нюанс в том, что он может быть < 0, если email
        if (!Peer.isGroupChat(message.peerId)) {
            append(message.peerId)
        }
        if (message.fwd != null) {
            val forwardMessages: List<Message> = message.fwd
            for (fwd in forwardMessages) {
                append(fwd)
            }
        }
        return this
    }

    fun appendNews(news: VKApiNews): VKOwnIds {
        append(news.source_id)
        append(news.copy_owner_id)
        if (news.hasCopyHistory()) {
            for (post in news.copy_history) {
                append(post)
            }
        }
        if (news.hasAttachments()) {
            append(news.attachments)
        }
        if (news.friends.nonNullNoEmpty()) {
            appendAll(news.friends)
        }
        return this
    }

    fun append(post: VKApiPost): VKOwnIds {
        //append(post.owner_id);
        append(post.from_id)
        append(post.signer_id)
        append(post.created_by)
        if (post.copy_history != null) {
            for (copy in post.copy_history) {
                append(copy)
            }
        }
        return this
    }

    fun append(ownerId: Int) {
        if (ownerId == 0) return
        if (ownerId > 0) {
            appendUid(ownerId)
        } else {
            appendGid(ownerId)
        }
    }

    fun appendAll(ownerIds: IntArray?) {
        if (ownerIds != null) {
            for (id in ownerIds) {
                append(id)
            }
        }
    }

    private fun appendUid(uid: Int) {
        uids.add(uid)
    }

    private fun appendGid(gid: Int) {
        gids.add(abs(gid))
    }

    private fun constainsUids(): Boolean {
        return uids.isNotEmpty()
    }

    private fun constainsGids(): Boolean {
        return gids.isNotEmpty()
    }

    val isEmpty: Boolean
        get() = !constainsUids() && !constainsGids()

    fun nonEmpty(): Boolean {
        return constainsGids() || constainsUids()
    }

    override fun toString(): String {
        return "uids: $uids, gids: $gids"
    }

    companion object {
        fun fromPosts(posts: Collection<VKApiPost>): VKOwnIds {
            val ids = VKOwnIds()
            for (post in posts) {
                ids.append(post)
            }
            return ids
        }
    }

    init {
        uids = HashSet()
        gids = HashSet()
    }
}