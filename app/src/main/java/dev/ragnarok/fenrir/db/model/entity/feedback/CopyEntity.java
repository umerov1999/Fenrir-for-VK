package dev.ragnarok.fenrir.db.model.entity.feedback;

import com.google.gson.annotations.SerializedName;

import dev.ragnarok.fenrir.db.model.entity.CopiesEntity;
import dev.ragnarok.fenrir.db.model.entity.Entity;
import dev.ragnarok.fenrir.db.model.entity.EntityWrapper;
import dev.ragnarok.fenrir.model.feedback.FeedbackType;

public class CopyEntity extends FeedbackEntity {

    @SerializedName("copies")
    private CopiesEntity copies;
    @SerializedName("copied")
    private EntityWrapper copied = new EntityWrapper();

    @SuppressWarnings("unused")
    public CopyEntity() {
    }

    public CopyEntity(@FeedbackType int type) {
        setType(type);
    }

    public CopiesEntity getCopies() {
        return copies;
    }

    public CopyEntity setCopies(CopiesEntity copies) {
        this.copies = copies;
        return this;
    }

    public Entity getCopied() {
        return copied.get();
    }

    public CopyEntity setCopied(Entity copied) {
        this.copied = new EntityWrapper().wrap(copied);
        return this;
    }
}