package dev.ragnarok.fenrir.api.model.longpoll;

public class MessageFlagsSetUpdate extends AbsLongpollEvent {

    //[[2,1030891,128,216143660]]

    public int message_id;
    public int mask;
    public int peer_id;

    public MessageFlagsSetUpdate() {
        super(ACTION_MESSAGES_FLAGS_SET);
    }

    public int getMessageId() {
        return message_id;
    }

    public int getPeerId() {
        return peer_id;
    }

    public int getMask() {
        return mask;
    }
}