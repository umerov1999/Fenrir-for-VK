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
        if (updates.output_messages_set_read_updates.nonNullNoEmpty() || updates.input_messages_set_read_updates.nonNullNoEmpty()) {
            completable = completable.andThen(
                messagesInteractor.handleReadUpdates(
                    accountId,
                    updates.output_messages_set_read_updates,
                    updates.input_messages_set_read_updates
                )
            )
        }
        if (updates.message_flags_reset_updates.nonNullNoEmpty() || updates.message_flags_set_updates.nonNullNoEmpty()) {
            completable = completable.andThen(
                messagesInteractor.handleFlagsUpdates(
                    accountId,
                    updates.message_flags_set_updates,
                    updates.message_flags_reset_updates
                )
            )
        }
        if (updates.badge_count_change_updates.nonNullNoEmpty()) {
            completable = completable.andThen(
                messagesInteractor.handleUnreadBadgeUpdates(
                    accountId,
                    updates.badge_count_change_updates
                )
            )
        }
        if (updates.typing_message_or_uploading_in_dialog_updates.nonNullNoEmpty()) {
            completable = completable.andThen(
                messagesInteractor.handleTypingMessageOrUploadingInDialogUpdates(
                    accountId,
                    updates.typing_message_or_uploading_in_dialog_updates
                )
            )
        }
        if (updates.message_reaction_changed_updates.nonNullNoEmpty()) {
            completable = completable.andThen(
                messagesInteractor.handleMessageReactionsChangedUpdates(
                    accountId,
                    updates.message_reaction_changed_updates
                )
            )
        }
        return completable
    }

}