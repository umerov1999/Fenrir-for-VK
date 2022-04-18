package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

public class VKApiDialog {

    @SerializedName("conversation")
    public VKApiConversation conversation;

    @SerializedName("last_message")
    public VKApiMessage lastMessage;
}