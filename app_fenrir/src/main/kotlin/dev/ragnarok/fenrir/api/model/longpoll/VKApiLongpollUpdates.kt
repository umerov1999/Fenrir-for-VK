package dev.ragnarok.fenrir.api.model.longpoll

import dev.ragnarok.fenrir.api.adapters.LongpollUpdatesDtoAdapter
import dev.ragnarok.fenrir.util.Utils.safeCountOfMultiple
import kotlinx.serialization.Serializable

@Serializable(with = LongpollUpdatesDtoAdapter::class)
class VKApiLongpollUpdates {
    // TODO Message edit update
    //{"ts":1841741106,"updates":[[5,1200880,51,26632922,1528116889,"и тд",{"title":""},{},1000771599]]}
    var ts: Long = 0
    var failed = 0

    var addMessageUpdates: MutableList<AddMessageUpdate>? = null

    var typingMessageOrUploadingInDialogUpdates: MutableList<TypingMessageOrUploadingInDialogUpdate>? =
        null
    var messageFlagsResetUpdates: MutableList<MessageFlagsResetUpdate>? = null
    var messageFlagsSetUpdates: MutableList<MessageFlagsSetUpdate>? = null
    var inputMessagesSetReadUpdates: MutableList<InputMessagesSetReadUpdate>? = null
    var outputMessagesSetReadUpdates: MutableList<OutputMessagesSetReadUpdate>? = null
    var badgeCountChangeUpdates: MutableList<BadgeCountChangeUpdate>? = null
    var messageReactionChangedUpdates: MutableList<ReactionMessageChangeUpdate>? = null
    var pinConversationUpdates: MutableList<PinConversationUpdate>? = null

    fun isOnlyAddMessages(): Boolean {
        return typingMessageOrUploadingInDialogUpdates.isNullOrEmpty() &&
                messageFlagsResetUpdates.isNullOrEmpty() &&
                messageFlagsSetUpdates.isNullOrEmpty() &&
                inputMessagesSetReadUpdates.isNullOrEmpty() &&
                outputMessagesSetReadUpdates.isNullOrEmpty() &&
                badgeCountChangeUpdates.isNullOrEmpty() &&
                messageReactionChangedUpdates.isNullOrEmpty() &&
                pinConversationUpdates.isNullOrEmpty()
    }

    //public VkApiLongpollUpdates(int account_id) {
    //    this.account_id = account_id;
    //}
    fun putUpdate(update: AbsLongpollEvent) {
        when (update.action) {
            AbsLongpollEvent.ACTION_MESSAGES_FLAGS_SET -> messageFlagsSetUpdates =
                addAndReturn(messageFlagsSetUpdates, update as MessageFlagsSetUpdate)

            AbsLongpollEvent.ACTION_MESSAGES_FLAGS_RESET -> messageFlagsResetUpdates =
                addAndReturn(messageFlagsResetUpdates, update as MessageFlagsResetUpdate)

            AbsLongpollEvent.ACTION_MESSAGE_EDITED, AbsLongpollEvent.ACTION_MESSAGE_CHANGED, AbsLongpollEvent.ACTION_MESSAGE_ADDED -> addMessageUpdates =
                addAndReturn(addMessageUpdates, update as AddMessageUpdate)

            AbsLongpollEvent.ACTION_MESSAGE_REACTION_CHANGE -> messageReactionChangedUpdates =
                addAndReturn(
                    messageReactionChangedUpdates,
                    update as ReactionMessageChangeUpdate
                )

            AbsLongpollEvent.ACTION_USER_TYPING_TEXT_IN_DIALOG,
            AbsLongpollEvent.ACTION_USER_RECORDING_VOICE_IN_DIALOG,
            AbsLongpollEvent.ACTION_USER_UPLOADING_PHOTO_IN_DIALOG,
            AbsLongpollEvent.ACTION_USER_UPLOADING_VIDEO_IN_DIALOG,
            AbsLongpollEvent.ACTION_USER_UPLOADING_FILE_IN_DIALOG -> typingMessageOrUploadingInDialogUpdates =
                addAndReturn(
                    typingMessageOrUploadingInDialogUpdates,
                    update as TypingMessageOrUploadingInDialogUpdate
                )

            AbsLongpollEvent.ACTION_SET_INPUT_MESSAGES_AS_READ -> inputMessagesSetReadUpdates =
                addAndReturn(inputMessagesSetReadUpdates, update as InputMessagesSetReadUpdate)

            AbsLongpollEvent.ACTION_SET_OUTPUT_MESSAGES_AS_READ -> outputMessagesSetReadUpdates =
                addAndReturn(
                    outputMessagesSetReadUpdates,
                    update as OutputMessagesSetReadUpdate
                )

            AbsLongpollEvent.ACTION_COUNTER_UNREAD_WAS_CHANGED -> badgeCountChangeUpdates =
                addAndReturn(badgeCountChangeUpdates, update as BadgeCountChangeUpdate)
            AbsLongpollEvent.ACTION_PIN_CONVERSATION -> pinConversationUpdates =
                addAndReturn(pinConversationUpdates, update as PinConversationUpdate)
        }
    }

    val updatesCount: Int
        get() = safeCountOfMultiple(
            typingMessageOrUploadingInDialogUpdates,
            addMessageUpdates,
            messageFlagsResetUpdates,
            messageFlagsSetUpdates,
            inputMessagesSetReadUpdates,
            outputMessagesSetReadUpdates,
            badgeCountChangeUpdates,
            messageReactionChangedUpdates,
            pinConversationUpdates
        )
    val isEmpty: Boolean
        get() = updatesCount == 0

    override fun toString(): String {
        return "Longpolling updates, count: $updatesCount, failed: $failed"
    }

    companion object {
        private fun <T : AbsLongpollEvent> crateAndAppend(item: T): MutableList<T> {
            val data: MutableList<T> = ArrayList(1)
            data.add(item)
            return data
        }

        internal fun <T : AbsLongpollEvent> addAndReturn(
            data: MutableList<T>?,
            item: T
        ): MutableList<T> {
            return if (data == null) {
                crateAndAppend(item)
            } else {
                data.add(item)
                data
            }
        }
    }
}
