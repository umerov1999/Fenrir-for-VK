package dev.ragnarok.fenrir.model;

import java.util.ArrayList;


public class UsersPart {

    public final ArrayList<User> users;
    public final int titleResId;
    public boolean enable;
    public Integer displayCount;

    public UsersPart(int titleResId, ArrayList<User> users, boolean enable) {
        this.titleResId = titleResId;
        this.users = users;
        this.enable = enable;
    }
}
