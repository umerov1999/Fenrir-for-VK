package dev.ragnarok.fenrir.db.model.entity;

import dev.ragnarok.fenrir.model.FavePageType;

public class FavePageEntity {
    private final int id;

    private String description;

    @FavePageType
    private String type;

    private long updateDate;

    private UserEntity user;

    private CommunityEntity group;

    public FavePageEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public FavePageEntity setDescription(String description) {
        this.description = description;
        return this;
    }


    public String getFaveType() {
        return type;
    }

    public FavePageEntity setFaveType(String type) {
        this.type = type;
        return this;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public FavePageEntity setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
        return this;
    }

    public UserEntity getUser() {
        return user;
    }

    public FavePageEntity setUser(UserEntity user) {
        this.user = user;
        return this;
    }

    public CommunityEntity getGroup() {
        return group;
    }

    public FavePageEntity setGroup(CommunityEntity group) {
        this.group = group;
        return this;
    }
}
