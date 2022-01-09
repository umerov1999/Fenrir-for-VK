package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.api.model.VkApiAttachments;

public class AttachmentsHistoryResponse {

    @SerializedName("items")
    public List<One> items;

    @SerializedName("next_from")
    public String next_from;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @SerializedName("groups")
    public List<VKApiCommunity> groups;

    public static class One {

        @SerializedName("message_id")
        public int messageId;

        @SerializedName("attachment")
        public VkApiAttachments.Entry entry;
    }
}
