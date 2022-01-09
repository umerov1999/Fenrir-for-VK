package dev.ragnarok.fenrir.db.model.entity.feedback;

import com.google.gson.annotations.SerializedName;

import dev.ragnarok.fenrir.model.feedback.FeedbackType;

public class UsersEntity extends FeedbackEntity {
    @SerializedName("ids")
    private int[] ids;

    @SuppressWarnings("unused")
    public UsersEntity() {
    }

    public UsersEntity(@FeedbackType int type) {
        setType(type);
    }

    public int[] getOwners() {
        return ids;
    }

    public UsersEntity setOwners(int[] ids) {
        this.ids = ids;
        return this;
    }
}