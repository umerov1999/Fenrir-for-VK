package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiConversation;
import dev.ragnarok.fenrir.api.model.VKApiMessage;
import dev.ragnarok.fenrir.api.model.VKApiUser;

public class MessageHistoryResponse {

    @Nullable
    @SerializedName("items")
    public List<VKApiMessage> messages;

    @SerializedName("count")
    public int count;

    @SerializedName("unread")
    public int unread;

    @Nullable
    @SerializedName("conversations")
    public List<VKApiConversation> conversations;

    @Nullable
    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @Nullable
    @SerializedName("groups")
    public List<VKApiCommunity> groups;
}
