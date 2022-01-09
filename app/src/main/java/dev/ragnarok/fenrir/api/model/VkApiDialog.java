package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

public class VkApiDialog {

    @SerializedName("conversation")
    public VkApiConversation conversation;

    @SerializedName("last_message")
    public VKApiMessage lastMessage;
}