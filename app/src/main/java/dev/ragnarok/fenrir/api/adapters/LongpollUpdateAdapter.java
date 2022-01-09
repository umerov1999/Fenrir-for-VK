package dev.ragnarok.fenrir.api.adapters;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.hasFlag;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

import dev.ragnarok.fenrir.api.model.VKApiMessage;
import dev.ragnarok.fenrir.api.model.VkApiConversation;
import dev.ragnarok.fenrir.api.model.longpoll.AbsLongpollEvent;
import dev.ragnarok.fenrir.api.model.longpoll.AddMessageUpdate;
import dev.ragnarok.fenrir.api.model.longpoll.BadgeCountChangeUpdate;
import dev.ragnarok.fenrir.api.model.longpoll.InputMessagesSetReadUpdate;
import dev.ragnarok.fenrir.api.model.longpoll.MessageFlagsResetUpdate;
import dev.ragnarok.fenrir.api.model.longpoll.MessageFlagsSetUpdate;
import dev.ragnarok.fenrir.api.model.longpoll.OutputMessagesSetReadUpdate;
import dev.ragnarok.fenrir.api.model.longpoll.UserIsOfflineUpdate;
import dev.ragnarok.fenrir.api.model.longpoll.UserIsOnlineUpdate;
import dev.ragnarok.fenrir.api.model.longpoll.WriteTextInDialogUpdate;
import dev.ragnarok.fenrir.api.util.VKStringUtils;
import dev.ragnarok.fenrir.model.Peer;

public class LongpollUpdateAdapter extends AbsAdapter implements JsonDeserializer<AbsLongpollEvent> {

    private static ArrayList<String> parseLineWithSeparators(String line, String separator) {
        if (isNull(line) || line.isEmpty()) {
            return null;
        }

        String[] tokens = line.split(separator);
        ArrayList<String> ids = new ArrayList<>();
        Collections.addAll(ids, tokens);
        return ids;
    }

    @Override
    public AbsLongpollEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        int action = array.get(0).getAsInt();
        return deserialize(action, array, context);
    }

    @Nullable
    private AbsLongpollEvent deserialize(int action, JsonArray array, JsonDeserializationContext context) {
        switch (action) {
            case AbsLongpollEvent.ACTION_MESSAGE_EDITED:
            case AbsLongpollEvent.ACTION_MESSAGE_CHANGED:
            case AbsLongpollEvent.ACTION_MESSAGE_ADDED:
                return deserializeAddMessageUpdate(array, context);

            case AbsLongpollEvent.ACTION_USER_WRITE_TEXT_IN_DIALOG:
                WriteTextInDialogUpdate w = new WriteTextInDialogUpdate(true);
                w.peer_id = optInt(array, 1);
                w.from_ids = optIntArray(array, 2, new int[]{});
                w.from_ids_count = optInt(array, 3);
                return w;

            case AbsLongpollEvent.ACTION_USER_WRITE_VOICE_IN_DIALOG:
                WriteTextInDialogUpdate v = new WriteTextInDialogUpdate(false);
                v.peer_id = optInt(array, 1);
                v.from_ids = optIntArray(array, 2, new int[]{});
                v.from_ids_count = optInt(array, 3);
                return v;

            case AbsLongpollEvent.ACTION_USER_IS_ONLINE:
                UserIsOnlineUpdate u = new UserIsOnlineUpdate();
                u.user_id = -optInt(array, 1);
                u.platform = optInt(array, 2);
                u.timestamp = optInt(array, 3);
                u.app_id = optInt(array, 4);
                return u;

            case AbsLongpollEvent.ACTION_USER_IS_OFFLINE:
                UserIsOfflineUpdate u1 = new UserIsOfflineUpdate();
                u1.user_id = -optInt(array, 1);
                u1.isTimeout = optInt(array, 2) != 0;
                u1.timestamp = optInt(array, 3);
                u1.app_id = optInt(array, 4);
                return u1;

            case AbsLongpollEvent.ACTION_MESSAGES_FLAGS_RESET: {
                MessageFlagsResetUpdate update = new MessageFlagsResetUpdate();
                update.message_id = optInt(array, 1);
                update.mask = optInt(array, 2);
                update.peer_id = optInt(array, 3);
                return update.peer_id != 0 && update.message_id != 0 ? update : null;
            }

            case AbsLongpollEvent.ACTION_MESSAGES_FLAGS_SET: {
                MessageFlagsSetUpdate update = new MessageFlagsSetUpdate();
                update.message_id = optInt(array, 1);
                update.mask = optInt(array, 2);
                update.peer_id = optInt(array, 3);
                return update.peer_id != 0 && update.message_id != 0 ? update : null;
            }

            case AbsLongpollEvent.ACTION_COUNTER_UNREAD_WAS_CHANGED:
                BadgeCountChangeUpdate c = new BadgeCountChangeUpdate();
                c.count = optInt(array, 1);
                return c;

            case AbsLongpollEvent.ACTION_SET_INPUT_MESSAGES_AS_READ: {
                InputMessagesSetReadUpdate update = new InputMessagesSetReadUpdate();
                update.peer_id = optInt(array, 1);
                update.local_id = optInt(array, 2);
                update.unread_count = optInt(array, 3); // undocumented
                return update.peer_id != 0 ? update : null;
            }
            case AbsLongpollEvent.ACTION_SET_OUTPUT_MESSAGES_AS_READ: {
                OutputMessagesSetReadUpdate update = new OutputMessagesSetReadUpdate();
                update.peer_id = optInt(array, 1);
                update.local_id = optInt(array, 2);
                update.unread_count = optInt(array, 3); // undocumented
                return update.peer_id != 0 ? update : null;
            }
        }

        return null;
    }

    private AddMessageUpdate deserializeAddMessageUpdate(JsonArray array, JsonDeserializationContext context) {
        AddMessageUpdate update = new AddMessageUpdate();

        int flags = optInt(array, 2);

        update.message_id = optInt(array, 1);
        update.peer_id = optInt(array, 3);
        update.timestamp = optLong(array, 4);
        update.text = VKStringUtils.unescape(optString(array, 5));
        update.outbox = hasFlag(flags, VKApiMessage.FLAG_OUTBOX);
        update.unread = hasFlag(flags, VKApiMessage.FLAG_UNREAD);
        update.important = hasFlag(flags, VKApiMessage.FLAG_IMPORTANT);
        update.deleted = hasFlag(flags, VKApiMessage.FLAG_DELETED);

        JsonObject extra = (JsonObject) opt(array, 6);
        if (nonNull(extra)) {
            update.from = optInt(extra, "from");
            update.sourceText = optString(extra, "source_text");
            update.sourceAct = optString(extra, "source_act");
            update.sourceMid = optInt(extra, "source_mid");
            update.payload = optString(extra, "payload");
            if (extra.has("keyboard")) {
                update.keyboard = context.deserialize(extra.get("keyboard"), VkApiConversation.CurrentKeyboard.class);
            }
        }

        JsonObject attachments = (JsonObject) opt(array, 7);
        if (nonNull(attachments)) {
            update.hasMedia = attachments.has("attach1_type");
            String fwd = optString(attachments, "fwd");
            String reply = optString(attachments, "reply");
            if (nonEmpty(fwd)) {
                update.fwds = parseLineWithSeparators(fwd, ",");
            }
            if (nonEmpty(reply)) {
                update.reply = reply;
            }
        }

        update.random_id = optString(array, 8); // ok
        update.edit_time = optLong(array, 10);

        if (update.from == 0 && !Peer.isGroupChat(update.peer_id) && !update.outbox) {
            update.from = update.peer_id;
        }

        return update.message_id != 0 ? update : null;
    }
}