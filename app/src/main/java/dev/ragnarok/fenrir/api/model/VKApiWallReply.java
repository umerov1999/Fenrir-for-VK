package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

public class VKApiWallReply implements VKApiAttachment {

    @SerializedName("id")
    public int id;

    @SerializedName("from_id")
    public int from_id;

    @SerializedName("post_id")
    public int post_id;

    @SerializedName("owner_id")
    public int owner_id;

    @SerializedName("text")
    public String text;

    @SerializedName("attachments")
    public VkApiAttachments attachments;

    public int getAttachmentsCount() {
        return attachments == null ? 0 : attachments.size();
    }

    public boolean hasAttachments() {
        return getAttachmentsCount() > 0;
    }

    @Override
    public String getType() {
        return TYPE_WALL_REPLY;
    }
}
