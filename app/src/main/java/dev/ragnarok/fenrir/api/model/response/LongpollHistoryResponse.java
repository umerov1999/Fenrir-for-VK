package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiMessage;

public class LongpollHistoryResponse {
    @Nullable
    @SerializedName("messages")
    public Messages messages;

    public static class Messages {

        @Nullable
        @SerializedName("items")
        public List<VKApiMessage> items;

    }

}
