package dev.ragnarok.fenrir.api.model.longpoll;

public class UserIsOnlineUpdate extends AbsLongpollEvent {

    public int user_id;
    public int platform;
    public int timestamp;
    public int app_id;

    public UserIsOnlineUpdate() {
        super(ACTION_USER_IS_ONLINE);
    }

    public int getUserId() {
        return user_id;
    }
}