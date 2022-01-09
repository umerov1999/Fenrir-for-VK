package dev.ragnarok.fenrir.model;

import dev.ragnarok.fenrir.api.model.Identificable;

public class FavePage implements Identificable {
    private final int id;

    private String description;

    private String type;

    private long updateDate;

    private User user;

    private Community group;

    public FavePage(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public FavePage setDescription(String description) {
        this.description = description;
        return this;
    }

    @FavePageType
    public String getType() {
        return type;
    }

    public FavePage setFaveType(@FavePageType String type) {
        this.type = type;
        return this;
    }

    public long getUpdatedDate() {
        return updateDate;
    }

    public FavePage setUpdatedDate(long updateDate) {
        this.updateDate = updateDate;
        return this;
    }

    public User getUser() {
        return user;
    }

    public FavePage setUser(User user) {
        this.user = user;
        return this;
    }

    public Community getGroup() {
        return group;
    }

    public FavePage setGroup(Community group) {
        this.group = group;
        return this;
    }

    public Owner getOwner() {
        switch (type) {
            case FavePageType.USER:
                return user;
            case FavePageType.COMMUNITY:
                return group;
        }
        return null;
    }
}

