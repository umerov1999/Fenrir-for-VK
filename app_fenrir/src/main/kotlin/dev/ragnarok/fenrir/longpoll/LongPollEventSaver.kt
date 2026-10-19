package dev.ragnarok.fenrir.longpoll

import dev.ragnarok.fenrir.api.model.longpoll.VKApiLongpollUpdates
import dev.ragnarok.fenrir.domain.IMessagesRepository
import dev.ragnarok.fenrir.domain.Repository.messages
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.andThen
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.emptyTaskFlow
import kotlinx.coroutines.flow.Flow

class LongPollEventSaver {
    private val messagesInteractor: IMessagesRepository = messages
    fun save(accountId: Long, updates: VKApiLongpollUpdates): Flow<Boolean> {
        var completable = emptyTaskFlow()
        if (updates.outputMessagesSetReadUpdates.nonNullNoEmpty() || updates.inputMessagesSetReadUpdates.nonNullNoEmpty()) {
            completable = completable.andThen(
                messagesInteractor.handleReadUpdates(
                    accountId,
                    updates.outputMessagesSetReadUpdates,
                    updates.inputMessagesSetReadUpdates
                )
            )
        }
        if (updates.messageFlagsResetUpdates.nonNullNoEmpty() || updates.messageFlagsSetUpdates.nonNullNoEmpty()) {
            completable = completable.andThen(
                messagesInteractor.handleFlagsUpdates(
                    accountId,
                    updates.messageFlagsSetUpdates,
                    updates.messageFlagsResetUpdates
                )
            )
        }
        if (updates.badgeCountChangeUpdates.nonNullNoEmpty()) {
            completable = completable.andThen(
                messagesInteractor.handleUnreadBadgeUpdates(
                    accountId,
                    updates.badgeCountChangeUpdates
                )
            )
        }
        if (updates.pinConversationUpdates.nonNullNoEmpty()) {
            completable = completable.andThen(
                messagesInteractor.handlePinConversationUpdates(
                    accountId,
                    updates.pinConversationUpdates
                )
            )
        }
        if (updates.typingMessageOrUploadingInDialogUpdates.nonNullNoEmpty()) {
            completable = completable.andThen(
                messagesInteractor.handleTypingMessageOrUploadingInDialogUpdates(
                    accountId,
                    updates.typingMessageOrUploadingInDialogUpdates
                )
            )
        }
        if (updates.messageReactionChangedUpdates.nonNullNoEmpty()) {
            completable = completable.andThen(
                messagesInteractor.handleMessageReactionsChangedUpdates(
                    accountId,
                    updates.messageReactionChangedUpdates
                )
            )
        }
        return completable
    }

}