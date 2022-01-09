package dev.ragnarok.fenrir.api.model.longpoll;

import android.text.TextUtils;

import java.util.ArrayList;

import dev.ragnarok.fenrir.api.model.VkApiConversation;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.util.Utils;

public class AddMessageUpdate extends AbsLongpollEvent {

    public int message_id;
    public long timestamp;
    public String text;
    public int from;
    public boolean outbox;
    public boolean unread;
    public boolean important;
    public boolean deleted;
    public boolean hasMedia;
    public String sourceText;
    public String sourceAct;
    public int sourceMid;
    public ArrayList<String> fwds;
    public VkApiConversation.CurrentKeyboard keyboard;
    public String payload;
    public String reply;
    public int peer_id;
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

    public String getText() {
        return text;
    }

    public String getPayload() {
        return payload;
    }

    public VkApiConversation.CurrentKeyboard getKeyboard() {
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
        return !TextUtils.isEmpty(sourceAct);
    }

    public boolean isFull() {
        return !hasMedia() && !hasFwds() && !hasReply() && !isServiceMessage();
    }

    public int getPeerId() {
        return peer_id;
    }
}