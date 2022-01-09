package dev.ragnarok.fenrir.longpoll;

import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.api.model.longpoll.VkApiLongpollUpdates;
import dev.ragnarok.fenrir.domain.IMessagesRepository;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.Repository;
import io.reactivex.rxjava3.core.Completable;

public class LongPollEventSaver {

    private final IMessagesRepository messagesInteractor;
    private final IOwnersRepository ownersRepository;

    public LongPollEventSaver() {
        messagesInteractor = Repository.INSTANCE.getMessages();
        ownersRepository = Repository.INSTANCE.getOwners();
    }

    public Completable save(int accountId, @NonNull VkApiLongpollUpdates updates) {
        Completable completable = Completable.complete();

        if (nonEmpty(updates.output_messages_set_read_updates) || nonEmpty(updates.input_messages_set_read_updates)) {
            completable = completable.andThen(messagesInteractor.handleReadUpdates(accountId, updates.output_messages_set_read_updates, updates.input_messages_set_read_updates));
        }

        if (nonEmpty(updates.message_flags_reset_updates) || nonEmpty(updates.message_flags_set_updates)) {
            completable = completable.andThen(messagesInteractor.handleFlagsUpdates(accountId, updates.message_flags_set_updates, updates.message_flags_reset_updates));
        }

        if (nonEmpty(updates.user_is_online_updates) || nonEmpty(updates.user_is_offline_updates)) {
            completable = completable.andThen(ownersRepository.handleOnlineChanges(accountId, updates.user_is_offline_updates, updates.user_is_online_updates));
        }

        if (nonEmpty(updates.badge_count_change_updates)) {
            completable = completable.andThen(messagesInteractor.handleUnreadBadgeUpdates(accountId, updates.badge_count_change_updates));
        }

        if (nonEmpty(updates.write_text_in_dialog_updates)) {
            completable = completable.andThen(messagesInteractor.handleWriteUpdates(accountId, updates.write_text_in_dialog_updates));
        }

        return completable;
    }
}