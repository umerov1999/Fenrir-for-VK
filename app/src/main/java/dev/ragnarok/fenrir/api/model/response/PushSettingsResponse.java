package dev.ragnarok.fenrir.api.model.response;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.util.Utils;


public class PushSettingsResponse {
    @SerializedName("conversations")
    public ConversationsPush conversations;

    public List<ConversationsPush.ConversationPushItem> getPushSettings() {
        if (nonNull(conversations) && !Utils.isEmpty(conversations.items)) {
            return conversations.items;
        }
        return Collections.emptyList();
    }

    public static class ConversationsPush {
        @SerializedName("items")
        List<ConversationPushItem> items;

        public static class ConversationPushItem {
            @SerializedName("disabled_until")
            public int disabled_until;
            @SerializedName("peer_id")
            public int peer_id;
        }
    }
}
