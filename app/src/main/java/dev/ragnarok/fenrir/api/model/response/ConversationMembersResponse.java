package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiConversationMembers;
import dev.ragnarok.fenrir.api.model.VKApiUser;

public class ConversationMembersResponse {

    @SerializedName("items")
    public List<VKApiConversationMembers> conversationMembers;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @SerializedName("groups")
    public List<VKApiCommunity> groups;
}
