package dev.ragnarok.fenrir.db.model.entity.feedback;

import com.google.gson.annotations.SerializedName;

import dev.ragnarok.fenrir.db.model.entity.Entity;
import dev.ragnarok.fenrir.db.model.entity.EntityWrapper;
import dev.ragnarok.fenrir.model.feedback.FeedbackType;

/**
 * Base class for types [mention]
 */
public class MentionEntity extends FeedbackEntity {

    @SerializedName("where")
    private EntityWrapper where = new EntityWrapper();

    @SuppressWarnings("unused")
    public MentionEntity() {
    }

    public MentionEntity(@FeedbackType int type) {
        setType(type);
    }

    public Entity getWhere() {
        return where.get();
    }

    public MentionEntity setWhere(Entity where) {
        this.where = new EntityWrapper().wrap(where);
        return this;
    }
}