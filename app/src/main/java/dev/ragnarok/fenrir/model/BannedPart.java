package dev.ragnarok.fenrir.model;

import java.util.List;


public class BannedPart {
    private final List<User> users;

    public BannedPart(List<User> users) {
        this.users = users;
    }

    public int getTotalCount() {
        return users.size();
    }

    public List<User> getUsers() {
        return users;
    }
}