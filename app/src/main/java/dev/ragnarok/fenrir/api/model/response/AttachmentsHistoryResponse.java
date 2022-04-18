package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiAttachments;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiUser;

public class AttachmentsHistoryResponse {

    @Nullable
    @SerializedName("items")
    public List<One> items;

    @Nullable
    @SerializedName("next_from")
    public String next_from;

    @Nullable
    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @Nullable
    @SerializedName("groups")
    public List<VKApiCommunity> groups;

    public static class One {

        @SerializedName("message_id")
        public int messageId;

        @Nullable
        @SerializedName("attachment")
        public VKApiAttachments.Entry entry;
    }
}
