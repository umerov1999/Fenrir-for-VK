package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiMessage;
import dev.ragnarok.fenrir.api.model.longpoll.AbsLongpollEvent;

public class LongpollHistoryResponse {

    @SerializedName("new_pts")
    public Long newPts;

    @SerializedName("history")
    public List<AbsLongpollEvent> history;

    @SerializedName("messages")
    public Messages messages;

    public static class Messages {

        @SerializedName("items")
        public List<VKApiMessage> items;

    }

}
