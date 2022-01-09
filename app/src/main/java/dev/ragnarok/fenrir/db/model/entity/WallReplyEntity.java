package dev.ragnarok.fenrir.db.model.entity;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.util.Utils;

@Keep
public class WallReplyEntity extends Entity {

    private int id;

    private int from_id;

    private int post_id;

    private int owner_id;

    private String text;

    private AttachmentsEntity attachments;

    public int getAttachmentsCount() {
        return attachments == null ? 0 : Utils.safeCountOf(attachments.getEntities());
    }

    public boolean hasAttachments() {
        return getAttachmentsCount() > 0;
    }

    public int getId() {
        return id;
    }

    public WallReplyEntity setId(int id) {
        this.id = id;
        return this;
    }

    public int getOwnerId() {
        return owner_id;
    }

    public WallReplyEntity setOwnerId(int owner_id) {
        this.owner_id = owner_id;
        return this;
    }

    public int getFromId() {
        return from_id;
    }

    public WallReplyEntity setFromId(int from_id) {
        this.from_id = from_id;
        return this;
    }

    public int getPostId() {
        return post_id;
    }

    public WallReplyEntity setPostId(int post_id) {
        this.post_id = post_id;
        return this;
    }

    public String getText() {
        return text;
    }

    public WallReplyEntity setText(String text) {
        this.text = text;
        return this;
    }

    public @Nullable
    List<Entity> getAttachments() {
        return nonNull(attachments) ? attachments.getEntities() : null;
    }

    public WallReplyEntity setAttachments(@Nullable List<Entity> entities) {
        attachments = AttachmentsEntity.from(entities);
        return this;
    }
}
