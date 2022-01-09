package dev.ragnarok.fenrir.api.model.longpoll;

public class OutputMessagesSetReadUpdate extends AbsLongpollEvent {

    public int peer_id;
    public int local_id;
    public int unread_count;

    public OutputMessagesSetReadUpdate() {
        super(ACTION_SET_OUTPUT_MESSAGES_AS_READ);
    }

    public int getPeerId() {
        return peer_id;
    }

    public int getLocalId() {
        return local_id;
    }

    public int getUnreadCount() {
        return unread_count;
    }
}