package dev.ragnarok.fenrir.api.model.longpoll;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import dev.ragnarok.fenrir.api.model.VKApiConversation;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.util.Utils;

public class AddMessageUpdate extends AbsLongpollEvent {

    public int message_id;
    public long timestamp;
    @Nullable
    public String text;
    public int from;
    public boolean outbox;
    public boolean unread;
    public boolean important;
    public boolean deleted;
    public boolean hasMedia;
    @Nullable
    public String sourceText;
    @Nullable
    public String sourceAct;
    public int sourceMid;
    @Nullable
    public ArrayList<String> fwds;
    @Nullable
    public VKApiConversation.CurrentKeyboard keyboard;
    @Nullable
    public String payload;
    @Nullable
    public String reply;
    public int peer_id;
    @Nullable
    public String random_id;
    public long edit_time;

    public AddMessageUpdate() {
        super(ACTION_MESSAGE_ADDED);
    }

    public int getMessageId() {
        return message_id;
    }

    public boolean isOut() {
        return outbox;
    }

    @Nullable
    public String getText() {
        return text;
    }

    @Nullable
    public String getPayload() {
        return payload;
    }

    @Nullable
    public VKApiConversation.CurrentKeyboard getKeyboard() {
        return keyboard;
    }

    public boolean isGroupChat() {
        return Peer.isGroupChat(peer_id);
    }

    public boolean hasMedia() {
        return hasMedia;
    }

    public boolean hasFwds() {
        return fwds != null && !fwds.isEmpty();
    }

    public boolean hasReply() {
        return !Utils.isEmpty(reply);
    }

    public boolean isServiceMessage() {
        return !Utils.isEmpty(sourceAct);
    }

    public boolean isFull() {
        return !hasMedia() && !hasFwds() && !hasReply() && !isServiceMessage();
    }

    public int getPeerId() {
        return peer_id;
    }
}