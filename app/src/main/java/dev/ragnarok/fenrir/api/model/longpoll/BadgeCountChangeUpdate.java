package dev.ragnarok.fenrir.api.model.longpoll;

public class BadgeCountChangeUpdate extends AbsLongpollEvent {

    public int count;

    public BadgeCountChangeUpdate() {
        super(ACTION_COUNTER_UNREAD_WAS_CHANGED);
    }

    public int getCount() {
        return count;
    }
}
