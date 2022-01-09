package dev.ragnarok.fenrir.api.model.longpoll;

import static dev.ragnarok.fenrir.api.model.longpoll.AbsLongpollEvent.ACTION_COUNTER_UNREAD_WAS_CHANGED;
import static dev.ragnarok.fenrir.api.model.longpoll.AbsLongpollEvent.ACTION_MESSAGES_FLAGS_RESET;
import static dev.ragnarok.fenrir.api.model.longpoll.AbsLongpollEvent.ACTION_MESSAGES_FLAGS_SET;
import static dev.ragnarok.fenrir.api.model.longpoll.AbsLongpollEvent.ACTION_MESSAGE_ADDED;
import static dev.ragnarok.fenrir.api.model.longpoll.AbsLongpollEvent.ACTION_MESSAGE_CHANGED;
import static dev.ragnarok.fenrir.api.model.longpoll.AbsLongpollEvent.ACTION_MESSAGE_EDITED;
import static dev.ragnarok.fenrir.api.model.longpoll.AbsLongpollEvent.ACTION_SET_INPUT_MESSAGES_AS_READ;
import static dev.ragnarok.fenrir.api.model.longpoll.AbsLongpollEvent.ACTION_SET_OUTPUT_MESSAGES_AS_READ;
import static dev.ragnarok.fenrir.api.model.longpoll.AbsLongpollEvent.ACTION_USER_IS_OFFLINE;
import static dev.ragnarok.fenrir.api.model.longpoll.AbsLongpollEvent.ACTION_USER_IS_ONLINE;
import static dev.ragnarok.fenrir.api.model.longpoll.AbsLongpollEvent.ACTION_USER_WRITE_TEXT_IN_DIALOG;
import static dev.ragnarok.fenrir.api.model.longpoll.AbsLongpollEvent.ACTION_USER_WRITE_VOICE_IN_DIALOG;
import static dev.ragnarok.fenrir.util.Utils.safeCountOfMultiple;

import java.util.ArrayList;
import java.util.List;

public final class VkApiLongpollUpdates {

    // TODO Message edit update
    //{"ts":1841741106,"updates":[[5,1200880,51,26632922,1528116889,"и тд",{"title":""},{},1000771599]]}

    public long ts;
    public int failed;

    public List<WriteTextInDialogUpdate> write_text_in_dialog_updates;
    public List<AddMessageUpdate> add_message_updates;
    public List<UserIsOnlineUpdate> user_is_online_updates;
    public List<UserIsOfflineUpdate> user_is_offline_updates;
    public List<MessageFlagsResetUpdate> message_flags_reset_updates;
    public List<MessageFlagsSetUpdate> message_flags_set_updates;
    public List<InputMessagesSetReadUpdate> input_messages_set_read_updates;
    public List<OutputMessagesSetReadUpdate> output_messages_set_read_updates;
    public List<BadgeCountChangeUpdate> badge_count_change_updates;

    public VkApiLongpollUpdates() {

    }

    private static <T extends AbsLongpollEvent> List<T> crateAndAppend(T item) {
        List<T> data = new ArrayList<>(1);
        data.add(item);
        return data;
    }

    private static <T extends AbsLongpollEvent> List<T> addAndReturn(List<T> data, T item) {
        if (data == null) {
            return crateAndAppend(item);
        } else {
            data.add(item);
            return data;
        }
    }

    //public VkApiLongpollUpdates(int account_id) {
    //    this.account_id = account_id;
    //}

    public void putUpdate(AbsLongpollEvent update) {
        switch (update.action) {
            case ACTION_MESSAGES_FLAGS_SET:
                message_flags_set_updates = addAndReturn(message_flags_set_updates, (MessageFlagsSetUpdate) update);
                break;

            case ACTION_MESSAGES_FLAGS_RESET:
                message_flags_reset_updates = addAndReturn(message_flags_reset_updates, (MessageFlagsResetUpdate) update);
                break;

            case ACTION_MESSAGE_EDITED:
            case ACTION_MESSAGE_CHANGED:
            case ACTION_MESSAGE_ADDED:
                add_message_updates = addAndReturn(add_message_updates, (AddMessageUpdate) update);
                break;

            case ACTION_USER_IS_ONLINE:
                user_is_online_updates = addAndReturn(user_is_online_updates, (UserIsOnlineUpdate) update);
                break;

            case ACTION_USER_IS_OFFLINE:
                user_is_offline_updates = addAndReturn(user_is_offline_updates, (UserIsOfflineUpdate) update);
                break;

            case ACTION_USER_WRITE_TEXT_IN_DIALOG:
            case ACTION_USER_WRITE_VOICE_IN_DIALOG:
                write_text_in_dialog_updates = addAndReturn(write_text_in_dialog_updates, (WriteTextInDialogUpdate) update);
                break;

            case ACTION_SET_INPUT_MESSAGES_AS_READ:
                input_messages_set_read_updates = addAndReturn(input_messages_set_read_updates, (InputMessagesSetReadUpdate) update);
                break;

            case ACTION_SET_OUTPUT_MESSAGES_AS_READ:
                output_messages_set_read_updates = addAndReturn(output_messages_set_read_updates, (OutputMessagesSetReadUpdate) update);
                break;

            case ACTION_COUNTER_UNREAD_WAS_CHANGED:
                badge_count_change_updates = addAndReturn(badge_count_change_updates, (BadgeCountChangeUpdate) update);
                break;
        }
    }

    public int getUpdatesCount() {
        return safeCountOfMultiple(
                write_text_in_dialog_updates,
                add_message_updates,
                user_is_online_updates,
                user_is_offline_updates,
                message_flags_reset_updates,
                message_flags_set_updates,
                input_messages_set_read_updates,
                output_messages_set_read_updates,
                badge_count_change_updates
        );
    }

    public boolean isEmpty() {
        return getUpdatesCount() == 0;
    }

    public List<WriteTextInDialogUpdate> getWriteTextInDialogUpdates() {
        return write_text_in_dialog_updates;
    }

    public List<AddMessageUpdate> getAddMessageUpdates() {
        return add_message_updates;
    }

    public List<UserIsOnlineUpdate> getUserIsOnlineUpdates() {
        return user_is_online_updates;
    }

    public List<UserIsOfflineUpdate> getUserIsOfflineUpdates() {
        return user_is_offline_updates;
    }

    public List<MessageFlagsResetUpdate> getMessageFlagsResetUpdates() {
        return message_flags_reset_updates;
    }

    public List<MessageFlagsSetUpdate> getMessageFlagsSetUpdates() {
        return message_flags_set_updates;
    }

    public List<InputMessagesSetReadUpdate> getInputMessagesSetReadUpdates() {
        return input_messages_set_read_updates;
    }

    public List<OutputMessagesSetReadUpdate> getOutputMessagesSetReadUpdates() {
        return output_messages_set_read_updates;
    }

    @Override
    public String toString() {
        return "Longpolling updates, count: " + getUpdatesCount() + ", failed: " + failed;
    }
}