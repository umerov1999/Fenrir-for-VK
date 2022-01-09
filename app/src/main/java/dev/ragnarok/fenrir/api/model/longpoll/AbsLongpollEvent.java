package dev.ragnarok.fenrir.api.model.longpoll;

public class AbsLongpollEvent {

    public static final int ACTION_MESSAGES_FLAGS_SET = 2;
    public static final int ACTION_MESSAGES_FLAGS_RESET = 3;
    public static final int ACTION_MESSAGE_ADDED = 4;
    public static final int ACTION_MESSAGE_EDITED = 5;
    public static final int ACTION_SET_INPUT_MESSAGES_AS_READ = 6;
    public static final int ACTION_SET_OUTPUT_MESSAGES_AS_READ = 7;
    public static final int ACTION_USER_IS_ONLINE = 8;
    public static final int ACTION_USER_IS_OFFLINE = 9;
    public static final int ACTION_MESSAGE_CHANGED = 18;
    public static final int ACTION_USER_WRITE_TEXT_IN_DIALOG = 63;
    public static final int ACTION_USER_WRITE_VOICE_IN_DIALOG = 64;
    public static final int ACTION_COUNTER_UNREAD_WAS_CHANGED = 80;

    public final int action;

    public AbsLongpollEvent(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }
}
